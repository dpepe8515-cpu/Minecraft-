package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgentLogEntity
import com.example.data.AgentTaskEntity
import com.example.data.BedrockAddonCompiler
import com.example.data.CatalogItemEntity
import com.example.data.ChatMessageEntity
import com.example.data.ChatThreadEntity
import com.example.data.MinecraftModEntity
import com.example.data.RoxyDatabase
import com.example.data.RoxyPreferencesRepository
import com.example.data.RoxyRemoteApi
import com.example.data.RoxySeedData
import com.example.data.UserPreferencesState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface FeedUiState<out T> {
    data object Loading : FeedUiState<Nothing>
    data class Success<T>(val items: T) : FeedUiState<T>
    data class Error(val message: String) : FeedUiState<Nothing>
}

enum class RoxyDestination {
    CHAT_GPT_STUDIO,
    MINECRAFT_MODS,
    HOME_AGENTS,
    EXPLORE_HUB,
    WORKSPACE_TERMINAL,
    PROFILE_SETTINGS
}

class RoxyViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = RoxyDatabase.getInstance(application).roxyDao()
    private val prefsRepo = RoxyPreferencesRepository(application)
    private val remoteApi = RoxyRemoteApi()

    val userPreferences: StateFlow<UserPreferencesState> = prefsRepo.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesState()
        )

    private val _currentDestination = MutableStateFlow(RoxyDestination.CHAT_GPT_STUDIO)
    val currentDestination: StateFlow<RoxyDestination> = _currentDestination.asStateFlow()

    // ChatGPT-Style Studio State
    val chatThreadsFlow: StateFlow<List<ChatThreadEntity>> = dao.observeChatThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatThreadId = MutableStateFlow(1L)
    val activeChatThreadId: StateFlow<Long> = _activeChatThreadId.asStateFlow()

    private val _activeThreadMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val activeThreadMessages: StateFlow<List<ChatMessageEntity>> = _activeThreadMessages.asStateFlow()

    private val _isLoadingChatThread = MutableStateFlow(true)
    val isLoadingChatThread: StateFlow<Boolean> = _isLoadingChatThread.asStateFlow()

    private val _isGeneratingChatResponse = MutableStateFlow(false)
    val isGeneratingChatResponse: StateFlow<Boolean> = _isGeneratingChatResponse.asStateFlow()

    val modsByIdFlow: StateFlow<Map<Long, MinecraftModEntity>> = dao.observeAllMinecraftMods()
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private var chatMessagesJob: Job? = null

    // Home Agent Feed filters & state
    private val _agentSearchQuery = MutableStateFlow("")
    val agentSearchQuery: StateFlow<String> = _agentSearchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _onlyBookmarked = MutableStateFlow(false)
    val onlyBookmarked: StateFlow<Boolean> = _onlyBookmarked.asStateFlow()

    private val _isHomeFeedLoading = MutableStateFlow(true)
    private val _homeFeedError = MutableStateFlow<String?>(null)

    val homeFeedState: StateFlow<FeedUiState<List<AgentTaskEntity>>> = combine(
        dao.observeAllTasks(),
        _agentSearchQuery,
        _selectedStatusFilter,
        _onlyBookmarked,
        combine(_isHomeFeedLoading, _homeFeedError) { loading, err -> loading to err }
    ) { tasks, query, status, bookmarkedOnly, (isLoading, errorMsg) ->
        if (isLoading) {
            FeedUiState.Loading
        } else if (errorMsg != null) {
            FeedUiState.Error(errorMsg)
        } else {
            val filtered = tasks.filter { task ->
                val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.repository.contains(query, ignoreCase = true) ||
                    task.worktreeBranch.contains(query, ignoreCase = true) ||
                    task.modelProvider.contains(query, ignoreCase = true)
                val matchesStatus = status == "ALL" || task.status.equals(status, ignoreCase = true)
                val matchesBookmark = !bookmarkedOnly || task.isBookmarked
                matchesQuery && matchesStatus && matchesBookmark
            }
            FeedUiState.Success(filtered)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedUiState.Loading
    )

    // Minecraft Bedrock Mod Studio Feed state
    private val _selectedModCategory = MutableStateFlow("ALL")
    val selectedModCategory: StateFlow<String> = _selectedModCategory.asStateFlow()

    private val _isModStudioLoading = MutableStateFlow(true)
    private val _isGeneratingMod = MutableStateFlow(false)
    val isGeneratingMod: StateFlow<Boolean> = _isGeneratingMod.asStateFlow()

    val modStudioFeedState: StateFlow<FeedUiState<List<MinecraftModEntity>>> = combine(
        dao.observeAllMinecraftMods(),
        _selectedModCategory,
        _isModStudioLoading
    ) { mods, cat, isLoading ->
        if (isLoading) {
            FeedUiState.Loading
        } else {
            val filtered = mods.filter { mod ->
                cat == "ALL" || mod.modCategory.equals(cat, ignoreCase = true)
            }
            FeedUiState.Success(filtered)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedUiState.Loading
    )

    // Explore Catalog Feed filters & state
    private val _catalogSearchQuery = MutableStateFlow("")
    val catalogSearchQuery: StateFlow<String> = _catalogSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isExploreLoading = MutableStateFlow(true)
    private val _exploreError = MutableStateFlow<String?>(null)
    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    val exploreFeedState: StateFlow<FeedUiState<List<CatalogItemEntity>>> = combine(
        dao.observeCatalogItems(),
        _catalogSearchQuery,
        _selectedCategory,
        _isExploreLoading,
        _exploreError
    ) { items, query, category, isLoading, errorMsg ->
        if (isLoading) {
            FeedUiState.Loading
        } else if (errorMsg != null) {
            FeedUiState.Error(errorMsg)
        } else {
            val filtered = items.filter { item ->
                val matchesCat = category == "ALL" || item.category.equals(category, ignoreCase = true)
                val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.provider.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.badgeText.contains(query, ignoreCase = true)
                matchesCat && matchesQuery
            }
            FeedUiState.Success(filtered)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedUiState.Loading
    )

    // Active Workspace Terminal Task & Logs
    private val _selectedTaskId = MutableStateFlow(1L)
    val selectedTaskId: StateFlow<Long> = _selectedTaskId.asStateFlow()

    private val _isWorkspaceLoading = MutableStateFlow(false)
    private val _isRunningPrompt = MutableStateFlow(false)
    val isRunningPrompt: StateFlow<Boolean> = _isRunningPrompt.asStateFlow()

    val allTasksFlow: StateFlow<List<AgentTaskEntity>> = dao.observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTaskLogs = MutableStateFlow<List<AgentLogEntity>>(emptyList())

    val workspaceFeedState: StateFlow<FeedUiState<List<AgentLogEntity>>> = combine(
        _activeTaskLogs,
        _isWorkspaceLoading
    ) { logs, isLoading ->
        if (isLoading) {
            FeedUiState.Loading
        } else {
            FeedUiState.Success(logs)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedUiState.Loading
    )

    private var logsObserverJob: Job? = null

    init {
        initializeSeedDataAndLoadFeeds()
        observeSelectedTaskLogs(1L)
        observeActiveChatThreadMessages(1L)
    }

    private fun initializeSeedDataAndLoadFeeds() {
        viewModelScope.launch {
            _isLoadingChatThread.value = true
            _isHomeFeedLoading.value = true
            _isModStudioLoading.value = true
            _isExploreLoading.value = true
            _isWorkspaceLoading.value = true

            val existingMods = dao.getAllMinecraftModsOnce()
            if (existingMods.isEmpty()) {
                dao.insertMinecraftMods(RoxySeedData.defaultMinecraftMods())
            }
            val existingThreads = dao.getChatThreadsOnce()
            if (existingThreads.isEmpty()) {
                dao.insertChatThread(RoxySeedData.defaultChatThreads()[0])
                dao.insertChatThread(RoxySeedData.defaultChatThreads()[1])
                dao.insertChatMessages(RoxySeedData.defaultMessagesForThread1())
                dao.insertChatMessages(RoxySeedData.defaultMessagesForThread2())
            }
            val existingTasks = dao.getAllTasksOnce()
            if (existingTasks.isEmpty()) {
                val seededTasks = RoxySeedData.defaultTasks()
                dao.insertTasks(seededTasks)
                seededTasks.forEach { task ->
                    dao.insertLogs(RoxySeedData.defaultLogsForTask(task.id))
                }
            }
            val existingCatalog = dao.getCatalogItemsOnce()
            if (existingCatalog.isEmpty()) {
                dao.insertCatalogItems(RoxySeedData.defaultCatalogItems())
            }

            val skeletonDelay = prefsRepo.preferencesFlow.first().skeletonDurationMs
            delay(skeletonDelay)
            _isLoadingChatThread.value = false
            _isHomeFeedLoading.value = false
            _isModStudioLoading.value = false
            _isExploreLoading.value = false
            _isWorkspaceLoading.value = false
        }
    }

    private fun observeActiveChatThreadMessages(threadId: Long) {
        chatMessagesJob?.cancel()
        chatMessagesJob = viewModelScope.launch {
            dao.observeMessagesForThread(threadId).collect { msgs ->
                _activeThreadMessages.value = msgs
            }
        }
    }

    private fun observeSelectedTaskLogs(taskId: Long) {
        logsObserverJob?.cancel()
        logsObserverJob = viewModelScope.launch {
            dao.observeLogsForTask(taskId).collect { logs ->
                _activeTaskLogs.value = logs
            }
        }
    }

    fun navigateTo(destination: RoxyDestination) {
        _currentDestination.value = destination
    }

    // ChatGPT-Style Studio Actions
    fun selectChatThread(threadId: Long) {
        if (_activeChatThreadId.value == threadId) return
        _activeChatThreadId.value = threadId
        viewModelScope.launch {
            _isLoadingChatThread.value = true
            observeActiveChatThreadMessages(threadId)
            val delayMs = (userPreferences.value.skeletonDurationMs * 0.5f).toLong().coerceAtLeast(280L)
            delay(delayMs)
            _isLoadingChatThread.value = false
        }
    }

    fun createNewChatThread() {
        viewModelScope.launch {
            val newId = dao.insertChatThread(
                ChatThreadEntity(
                    title = "Nuevo Mod / Sesión IA",
                    selectedModel = "RoxyGPT-4o • Mod Engine",
                    bedrockVersion = "1.21.70+"
                )
            )
            _activeChatThreadId.value = newId
            observeActiveChatThreadMessages(newId)
        }
    }

    fun deleteChatThread(threadId: Long) {
        viewModelScope.launch {
            dao.deleteMessagesForThread(threadId)
            dao.deleteChatThread(threadId)
            val remaining = dao.getChatThreadsOnce()
            if (remaining.isNotEmpty()) {
                selectChatThread(remaining.first().id)
            } else {
                createNewChatThread()
            }
        }
    }

    fun updateActiveThreadModelAndVersion(model: String, version: String) {
        viewModelScope.launch {
            val current = chatThreadsFlow.value.find { it.id == _activeChatThreadId.value } ?: return@launch
            dao.updateChatThread(
                current.copy(
                    selectedModel = model,
                    bedrockVersion = version,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _statusBannerMessage.value = "Configurado: $model • Minecraft Bedrock $version"
        }
    }

    fun previewChatSkeletonAnimation() {
        viewModelScope.launch {
            _isLoadingChatThread.value = true
            delay(userPreferences.value.skeletonDurationMs)
            _isLoadingChatThread.value = false
        }
    }

    fun sendChatGptPrompt(prompt: String) {
        if (prompt.isBlank() || _isGeneratingChatResponse.value) return
        val threadId = _activeChatThreadId.value
        viewModelScope.launch {
            _isGeneratingChatResponse.value = true

            // 1. Insert user message
            dao.insertChatMessage(
                ChatMessageEntity(
                    threadId = threadId,
                    role = "USER",
                    content = prompt.trim()
                )
            )

            // Update thread title if it was a fresh thread
            val currentThread = chatThreadsFlow.value.find { it.id == threadId }
            val bedrockVer = currentThread?.bedrockVersion ?: "1.21.70+"
            if (currentThread != null && (currentThread.title.startsWith("Nuevo") || _activeThreadMessages.value.size <= 1)) {
                val shortTitle = prompt.trim().take(34)
                dao.updateChatThread(currentThread.copy(title = shortTitle, updatedAt = System.currentTimeMillis()))
            }

            // Show ChatTurnSkeletonCard shimmer during synthesis & compilation
            val skeletonWait = (userPreferences.value.skeletonDurationMs * 0.85f).toLong().coerceAtLeast(450L)
            delay(skeletonWait)

            // 2. Synthesize custom Minecraft Bedrock Mod + .mcaddon package
            val lower = prompt.lowercase()
            val isWeapon = lower.contains("espada") || lower.contains("arma") || lower.contains("katana") || lower.contains("guadaña")
            val isBoss = lower.contains("boss") || lower.contains("jefe") || lower.contains("dragón")
            val category = when {
                isWeapon -> "WEAPON_ITEM"
                isBoss -> "MOB_BOSS"
                else -> "NPC_COMPANION"
            }

            val (primaryHex, secondaryHex) = when {
                lower.contains("rosa") || lower.contains("femboy") || lower.contains("astolfo") || lower.contains("pastel") ->
                    "#F472B6" to "#8B5CF6"
                lower.contains("cariñosa") || lower.contains("novia") || lower.contains("aliada") || lower.contains("guardiana") ->
                    "#EC4899" to "#00F0FF"
                isWeapon -> "#10B981" to "#F59E0B"
                else -> "#00F0FF" to "#8B5CF6"
            }

            val tameItem = when {
                lower.contains("flor") || lower.contains("rosa") -> "minecraft:poppy"
                lower.contains("amatista") -> "minecraft:amethyst_shard"
                lower.contains("manzana") -> "minecraft:golden_apple"
                else -> "minecraft:diamond"
            }

            val modDisplayName = when {
                lower.contains("femboy") -> "Felix • Compañero NPC Leal"
                lower.contains("cariñosa") || lower.contains("aliada") -> "Aria • Compañera & Guardiana"
                isWeapon -> "Hoja Mítica Roxy"
                isBoss -> "Titán Guardián Roxy"
                else -> prompt.trim().split(Regex("\\s+")).take(4).joinToString(" ").replaceFirstChar { it.uppercase() }
            }

            val slug = modDisplayName.lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .take(18)
                .ifBlank { "custom_companion" }

            val dialogues = buildString {
                appendLine("¡Siempre estaré a tu lado en este mundo de Minecraft!")
                appendLine("¡Déjame protegerte, ningún enemigo te hará daño mientras yo esté aquí!")
                appendLine("¡Muchas gracias por tu regalo, eres mi persona favorita!")
            }.trim()

            val createdMod = MinecraftModEntity(
                name = modDisplayName,
                entityIdentifier = "roxy:$slug",
                modCategory = category,
                bedrockVersion = bedrockVer,
                promptUsed = prompt.trim(),
                maxHealth = if (isBoss) 240 else 100,
                moveSpeed = 0.35,
                attackDamage = if (isWeapon) 24 else 15,
                tameItem = tameItem,
                followsPlayer = true,
                defendsPlayer = true,
                dialogueLines = dialogues,
                primaryColorHex = primaryHex,
                secondaryColorHex = secondaryHex
            )

            val newModId = dao.insertMinecraftMod(createdMod)
            val persistedMod = createdMod.copy(id = newModId)
            val entityJsonSnippet = BedrockAddonCompiler.generateBehaviorEntityJson(persistedMod).take(360) + "\n  ...\n}"

            val assistantReply = buildString {
                appendLine("¡He creado y compilado tu mod **${persistedMod.name}** (`${persistedMod.entityIdentifier}`) compatible con **Minecraft Bedrock $bedrockVer** en Android!")
                appendLine()
                appendLine("• **IA de Compañero**: Te sigue automáticamente (`minecraft:behavior.follow_owner`), abre puertas y te defiende en combate.")
                appendLine("• **Domesticación y Regalos**: Puedes darle `${persistedMod.tameItem}` o flores para activar sus diálogos personalizados.")
                appendLine("• **Textura PNG 64x64 + Behavior & Resource Pack**: Todo empaquetado con UUID v4 válidos.")
                append("Usa los botones de la tarjeta de abajo para **guardar el archivo `.mcaddon`** o **abrirlo directo en Minecraft**:")
            }

            dao.insertChatMessage(
                ChatMessageEntity(
                    threadId = threadId,
                    role = "ASSISTANT",
                    content = assistantReply,
                    linkedModId = newModId,
                    codeSnippet = entityJsonSnippet
                )
            )

            _isGeneratingChatResponse.value = false
        }
    }

    fun selectTaskAndOpenTerminal(taskId: Long) {
        _selectedTaskId.value = taskId
        _currentDestination.value = RoxyDestination.WORKSPACE_TERMINAL
        viewModelScope.launch {
            _isWorkspaceLoading.value = true
            observeSelectedTaskLogs(taskId)
            val skeletonDelay = (userPreferences.value.skeletonDurationMs * 0.65f).toLong().coerceAtLeast(320L)
            delay(skeletonDelay)
            _isWorkspaceLoading.value = false
        }
    }

    // Minecraft Bedrock Mod Studio Actions
    fun selectModStudioCategory(category: String) {
        if (_selectedModCategory.value == category) return
        _selectedModCategory.value = category
        viewModelScope.launch {
            _isModStudioLoading.value = true
            val duration = (userPreferences.value.skeletonDurationMs * 0.55f).toLong().coerceAtLeast(300L)
            delay(duration)
            _isModStudioLoading.value = false
        }
    }

    fun refreshModStudioFeed() {
        viewModelScope.launch {
            _isModStudioLoading.value = true
            delay(userPreferences.value.skeletonDurationMs)
            _isModStudioLoading.value = false
        }
    }

    fun generateAiMinecraftMod(
        prompt: String,
        customName: String,
        category: String,
        bedrockVersion: String,
        primaryHex: String,
        secondaryHex: String
    ) {
        viewModelScope.launch {
            _isGeneratingMod.value = true
            val resolvedName = customName.trim().ifBlank {
                prompt.trim().split(Regex("\\s+")).take(4).joinToString(" ").replaceFirstChar { it.uppercase() }
                    .ifBlank { "Compañero NPC Roxy" }
            }
            val slug = resolvedName.lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .take(20)
                .ifBlank { "custom_companion" }

            val lowerPrompt = prompt.lowercase()
            val tameItem = when {
                lowerPrompt.contains("flor") || lowerPrompt.contains("rosa") -> "minecraft:poppy"
                lowerPrompt.contains("amatista") -> "minecraft:amethyst_shard"
                lowerPrompt.contains("manzana") -> "minecraft:golden_apple"
                lowerPrompt.contains("pastel") || lowerPrompt.contains("tarta") -> "minecraft:cake"
                else -> "minecraft:diamond"
            }

            val dialogues = buildString {
                appendLine("¡Hola! Soy $resolvedName y siempre iré contigo en cada aventura.")
                appendLine("¡No te preocupes, yo te protegeré de cualquier mob que se acerque!")
                appendLine("¡Muchas gracias por el regalo! Me alegra muchísimo acompañarte.")
            }.trim()

            val newMod = MinecraftModEntity(
                name = resolvedName,
                entityIdentifier = "roxy:$slug",
                modCategory = category,
                bedrockVersion = bedrockVersion,
                promptUsed = prompt.ifBlank { "Mod personalizado generado con IA para Minecraft Bedrock ($bedrockVersion)." },
                maxHealth = if (category == "MOB_BOSS") 250 else 100,
                moveSpeed = 0.35,
                attackDamage = if (category == "WEAPON_ITEM") 22 else 14,
                tameItem = tameItem,
                followsPlayer = true,
                defendsPlayer = true,
                dialogueLines = dialogues,
                primaryColorHex = primaryHex,
                secondaryColorHex = secondaryHex
            )

            dao.insertMinecraftMod(newMod)
            _isGeneratingMod.value = false
            _statusBannerMessage.value =
                "Mod '${newMod.name}' (${newMod.entityIdentifier}) compilado para Bedrock $bedrockVersion. ¡Listo para exportar como .mcaddon!"
        }
    }

    fun updateMinecraftMod(mod: MinecraftModEntity) {
        viewModelScope.launch {
            dao.updateMinecraftMod(mod)
            _statusBannerMessage.value = "Mod '${mod.name}' actualizado y recompilado"
        }
    }

    fun cycleModSkinPalette(mod: MinecraftModEntity) {
        val palettes = listOf(
            "#F472B6" to "#8B5CF6",
            "#00F0FF" to "#EC4899",
            "#10B981" to "#F59E0B",
            "#F43F5E" to "#1E293B",
            "#A855F7" to "#38BDF8"
        )
        val currentIdx = palettes.indexOfFirst {
            it.first.equals(mod.primaryColorHex, ignoreCase = true)
        }
        val (nextPrimary, nextSecondary) = palettes[(currentIdx + 1) % palettes.size]
        viewModelScope.launch {
            dao.updateMinecraftMod(
                mod.copy(
                    primaryColorHex = nextPrimary,
                    secondaryColorHex = nextSecondary
                )
            )
            _statusBannerMessage.value =
                "Skin 64x64 de '${mod.name}' actualizada ($nextPrimary / $nextSecondary)"
        }
    }

    fun deleteMinecraftMod(modId: Long) {
        viewModelScope.launch {
            dao.deleteMinecraftMod(modId)
            _statusBannerMessage.value = "Mod eliminado de tu biblioteca"
        }
    }

    fun exportMcAddonToUri(mod: MinecraftModEntity, destinationUri: Uri) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val bytes = BedrockAddonCompiler.compileMcAddonBytes(mod)
                    getApplication<Application>().contentResolver.openOutputStream(destinationUri)?.use { out ->
                        out.write(bytes)
                    }
                    Result.success(bytes.size)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            result.onSuccess { byteCount ->
                _statusBannerMessage.value =
                    "Archivo .mcaddon guardado (${byteCount / 1024} KB). Ábrelo desde tus archivos para importarlo en Minecraft Bedrock."
            }.onFailure { err ->
                _statusBannerMessage.value =
                    "Error al guardar .mcaddon: ${err.localizedMessage ?: "Permiso denegado"}"
            }
        }
    }

    fun shareMcAddonToMinecraft(mod: MinecraftModEntity) {
        viewModelScope.launch {
            val result = BedrockAddonCompiler.shareOrOpenMcAddonInMinecraft(
                context = getApplication(),
                mod = mod
            )
            result.onSuccess { fileName ->
                _statusBannerMessage.value = "Exportando $fileName hacia Minecraft Bedrock…"
            }.onFailure { err ->
                _statusBannerMessage.value =
                    "No se pudo abrir el selector: ${err.localizedMessage ?: "Error"}"
            }
        }
    }

    // Home Feed Actions
    fun updateAgentSearchQuery(query: String) {
        _agentSearchQuery.value = query
    }

    fun selectStatusFilter(status: String) {
        if (_selectedStatusFilter.value == status) return
        _selectedStatusFilter.value = status
        triggerHomeFeedSkeletonTransition(factor = 0.55f)
    }

    fun toggleBookmarkedFilter() {
        _onlyBookmarked.value = !_onlyBookmarked.value
    }

    fun resetHomeFilters() {
        _agentSearchQuery.value = ""
        _selectedStatusFilter.value = "ALL"
        _onlyBookmarked.value = false
        _homeFeedError.value = null
        triggerHomeFeedSkeletonTransition(factor = 0.6f)
    }

    fun refreshHomeFeed(simulateError: Boolean = false) {
        viewModelScope.launch {
            _homeFeedError.value = null
            _isHomeFeedLoading.value = true
            delay(userPreferences.value.skeletonDurationMs)
            if (simulateError) {
                _homeFeedError.value =
                    "No se pudo sincronizar el estado de los worktrees remotos de Roxy.gg. Verifica tu conexión o reintenta."
            }
            _isHomeFeedLoading.value = false
        }
    }

    private fun triggerHomeFeedSkeletonTransition(factor: Float = 0.6f) {
        viewModelScope.launch {
            _isHomeFeedLoading.value = true
            val duration = (userPreferences.value.skeletonDurationMs * factor).toLong().coerceAtLeast(300L)
            delay(duration)
            _isHomeFeedLoading.value = false
        }
    }

    fun toggleTaskBookmark(task: AgentTaskEntity) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isBookmarked = !task.isBookmarked))
        }
    }

    fun createNewAgentTask(
        title: String,
        repository: String,
        modelName: String,
        initialPrompt: String
    ) {
        viewModelScope.launch {
            val sanitizedBranch = "wt/" + title.lowercase()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
                .take(22)
                .ifBlank { "parallel-task" }

            val newTask = AgentTaskEntity(
                title = title.ifBlank { "Agente de refactorización autónoma" },
                repository = repository.ifBlank { "roxy-gg/roxy" },
                worktreeBranch = sanitizedBranch,
                modelProvider = "roxy-v1 / $modelName",
                status = "RUNNING",
                progressPercent = 25,
                tokensUsed = 1420,
                latencyMs = 260,
                filesChanged = 1,
                lastStepSummary = "Worktree creado ($sanitizedBranch). Ejecutando prompt: ${initialPrompt.take(70)}",
                isBookmarked = true
            )
            val newId = dao.insertTask(newTask)
            dao.insertLogs(
                listOf(
                    AgentLogEntity(
                        taskId = newId,
                        stepType = "WORKTREE",
                        title = "Worktree aislado creado ($sanitizedBranch)",
                        detail = "Copia independiente del repositorio $repository lista para ejecución paralela."
                    ),
                    AgentLogEntity(
                        taskId = newId,
                        stepType = "PLAN",
                        title = "Prompt inicial recibido",
                        detail = initialPrompt.ifBlank { "Analizar arquitectura y proponer mejoras." }
                    )
                )
            )
            _statusBannerMessage.value = "Agente paralelo lanzado en rama $sanitizedBranch"
            selectTaskAndOpenTerminal(newId)
        }
    }

    fun deleteAgentTask(taskId: Long) {
        viewModelScope.launch {
            dao.deleteTask(taskId)
            _statusBannerMessage.value = "Sesión de agente eliminada del historial"
        }
    }

    // Explore Catalog Actions
    fun updateCatalogSearchQuery(query: String) {
        _catalogSearchQuery.value = query
    }

    fun selectCatalogCategory(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        viewModelScope.launch {
            _exploreError.value = null
            _isExploreLoading.value = true
            val duration = (userPreferences.value.skeletonDurationMs * 0.55f).toLong().coerceAtLeast(300L)
            delay(duration)
            _isExploreLoading.value = false
        }
    }

    fun refreshExploreCatalog() {
        viewModelScope.launch {
            _exploreError.value = null
            _isExploreLoading.value = true
            delay(userPreferences.value.skeletonDurationMs)
            _isExploreLoading.value = false
        }
    }

    fun searchLiveGitHubRepositories(query: String) {
        viewModelScope.launch {
            _exploreError.value = null
            _isExploreLoading.value = true
            val searchTopic = query.ifBlank { "roxy-gg OR minecraft-bedrock-addon" }
            val result = remoteApi.searchGitHubRepositories(searchTopic)
            result.onSuccess { repos ->
                if (repos.isNotEmpty()) {
                    val entities = repos.map { repo ->
                        CatalogItemEntity(
                            id = repo.id,
                            title = repo.fullName,
                            category = "GITHUB_REPO",
                            provider = "GitHub Live API • ${repo.language}",
                            badgeText = "★ ${repo.stars}",
                            metricLabel = "Worktree Ready",
                            description = repo.description,
                            endpointOrCommand = repo.htmlUrl,
                            isInstalled = false,
                            starCount = repo.stars
                        )
                    }
                    dao.insertCatalogItems(entities)
                    _selectedCategory.value = "GITHUB_REPO"
                    _statusBannerMessage.value =
                        "Se sincronizaron ${entities.size} repositorios reales desde la API de GitHub."
                } else {
                    _statusBannerMessage.value = "GitHub API no devolvió resultados para '$searchTopic'."
                }
            }.onFailure { err ->
                _statusBannerMessage.value =
                    "Modo local activo (GitHub API: ${err.localizedMessage?.take(60) ?: "sin red"})."
            }
            _isExploreLoading.value = false
        }
    }

    fun toggleCatalogItemInstalled(item: CatalogItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isInstalled = !item.isInstalled)
            dao.updateCatalogItem(updated)
            _statusBannerMessage.value = if (updated.isInstalled) {
                "'${updated.title}' activado en el entorno Roxy"
            } else {
                "'${updated.title}' desactivado"
            }
        }
    }

    // Workspace Terminal Actions
    fun refreshWorkspaceTerminal() {
        viewModelScope.launch {
            _isWorkspaceLoading.value = true
            delay(userPreferences.value.skeletonDurationMs)
            _isWorkspaceLoading.value = false
        }
    }

    fun executeTerminalPrompt(prompt: String) {
        if (prompt.isBlank() || _isRunningPrompt.value) return
        val taskId = _selectedTaskId.value
        viewModelScope.launch {
            _isRunningPrompt.value = true
            val currentTask = allTasksFlow.value.find { it.id == taskId }
            val prefs = userPreferences.value

            dao.insertLog(
                AgentLogEntity(
                    taskId = taskId,
                    stepType = "TOOL_CALL",
                    title = "Prompt enviado a ${prefs.defaultModel}",
                    detail = "> $prompt"
                )
            )

            val response = remoteApi.runOpenAiCompatiblePrompt(
                baseUrl = prefs.apiBaseUrl,
                apiKey = prefs.apiKey,
                model = prefs.defaultModel,
                prompt = prompt,
                repoContext = currentTask?.repository ?: "roxy-gg/roxy"
            )

            dao.insertLog(
                AgentLogEntity(
                    taskId = taskId,
                    stepType = if (response.success) "RESPONSE" else "DIFF",
                    title = "${response.modelUsed} • ${response.latencyMs}ms",
                    detail = response.content
                )
            )

            if (currentTask != null) {
                val nextProgress = (currentTask.progressPercent + 15).coerceAtMost(100)
                val nextStatus = if (nextProgress >= 100) "COMPLETED" else "RUNNING"
                dao.updateTask(
                    currentTask.copy(
                        progressPercent = nextProgress,
                        status = nextStatus,
                        tokensUsed = currentTask.tokensUsed + 640,
                        latencyMs = response.latencyMs.toInt().coerceAtLeast(180),
                        lastStepSummary = response.content.lineSequence().firstOrNull()?.take(90)
                            ?: currentTask.lastStepSummary
                    )
                )
            }
            _isRunningPrompt.value = false
        }
    }

    // Profile & Preferences Actions
    fun saveProfileAndApiSettings(
        handle: String,
        baseUrl: String,
        apiKey: String,
        defaultModel: String
    ) {
        viewModelScope.launch {
            prefsRepo.updateDeveloperHandle(handle)
            prefsRepo.updateApiConfig(baseUrl, apiKey, defaultModel)
            _statusBannerMessage.value = "Configuración de perfil y endpoint OpenAI guardada"
        }
    }

    fun updateAccentTheme(index: Int) {
        viewModelScope.launch {
            prefsRepo.updateAccentTheme(index)
        }
    }

    fun updateSkeletonPreferences(durationMs: Long, reduceMotion: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateSkeletonSettings(durationMs, reduceMotion)
        }
    }

    fun updateAutoIsolateWorktrees(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateAutoIsolateWorktrees(enabled)
        }
    }

    fun dismissBannerMessage() {
        _statusBannerMessage.value = null
    }
}
