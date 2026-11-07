package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChatMessageEntity
import com.example.data.ChatThreadEntity
import com.example.data.MinecraftModEntity
import com.example.ui.components.ChatTurnSkeletonCard
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardElevated
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.RoxyEmerald
import com.example.ui.theme.RoxyObsidian
import com.example.ui.theme.RoxyRose
import com.example.ui.theme.RoxyTextMuted
import com.example.ui.theme.RoxyTextSecondary
import com.example.viewmodel.RoxyDestination
import kotlinx.coroutines.launch

@Composable
fun ChatGptStudioScreen(
    threads: List<ChatThreadEntity>,
    activeThread: ChatThreadEntity?,
    messages: List<ChatMessageEntity>,
    modsById: Map<Long, MinecraftModEntity>,
    isLoadingThread: Boolean,
    isGeneratingResponse: Boolean,
    reduceMotion: Boolean,
    onSelectThread: (Long) -> Unit,
    onCreateNewThread: () -> Unit,
    onDeleteThread: (Long) -> Unit,
    onUpdateThreadModelAndVersion: (String, String) -> Unit,
    onSendChatPrompt: (String) -> Unit,
    onCycleModSkin: (MinecraftModEntity) -> Unit,
    onRefreshSkeletonPreview: () -> Unit,
    onSaveMcAddonToUri: (MinecraftModEntity, Uri) -> Unit,
    onShareMcAddon: (MinecraftModEntity) -> Unit,
    onNavigateToDestination: (RoxyDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var composerText by rememberSaveable { mutableStateOf("") }
    var drawerSearchQuery by rememberSaveable { mutableStateOf("") }
    var showModelPickerDialog by rememberSaveable { mutableStateOf(false) }
    var pendingSaveMod by remember { mutableStateOf<MinecraftModEntity?>(null) }

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val modToSave = pendingSaveMod
        if (uri != null && modToSave != null) {
            onSaveMcAddonToUri(modToSave, uri)
        }
        pendingSaveMod = null
    }

    LaunchedEffect(messages.size, isGeneratingResponse) {
        if (messages.isNotEmpty() || isGeneratingResponse) {
            val targetIdx = (messages.size + if (isGeneratingResponse) 1 else 0).coerceAtLeast(1) - 1
            listState.animateScrollToItem(targetIdx)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = RoxyObsidian,
                modifier = Modifier.width(305.dp)
            ) {
                ChatGptDrawerContent(
                    threads = threads,
                    activeThreadId = activeThread?.id,
                    searchQuery = drawerSearchQuery,
                    onSearchChange = { drawerSearchQuery = it },
                    onNewChatClick = {
                        onCreateNewThread()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectThread = { id ->
                        onSelectThread(id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteThread = onDeleteThread,
                    onNavigateToSection = { dest ->
                        onNavigateToDestination(dest)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top ChatGPT-Style Header Bar
            ChatGptTopBar(
                modelLabel = activeThread?.selectedModel ?: "RoxyGPT-4o • Mod Engine",
                bedrockVersion = activeThread?.bedrockVersion ?: "1.21.70+",
                onOpenDrawer = {
                    coroutineScope.launch { drawerState.open() }
                },
                onOpenModelSelector = { showModelPickerDialog = true },
                onNewChatClick = onCreateNewThread,
                onRefreshSkeletonClick = onRefreshSkeletonPreview
            )

            // Main Conversation Stream or Zero-State Starters
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(
                    targetState = isLoadingThread,
                    animationSpec = tween(durationMillis = 220),
                    label = "chat_thread_crossfade"
                ) { loadingThread ->
                    if (loadingThread) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .testTag("chat_loading_skeleton_list"),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(2) {
                                ChatTurnSkeletonCard(reduceMotion = reduceMotion)
                            }
                        }
                    } else if (messages.isEmpty() && !isGeneratingResponse) {
                        ChatGptZeroStateWelcome(
                            bedrockVersion = activeThread?.bedrockVersion ?: "1.21.70+",
                            onPromptStarterClick = { starterPrompt ->
                                onSendChatPrompt(starterPrompt)
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .testTag("chat_messages_list"),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = messages,
                                key = { it.id },
                                contentType = { it.role }
                            ) { msg ->
                                if (msg.role == "USER") {
                                    UserMessageBubble(message = msg)
                                } else {
                                    val linkedMod = msg.linkedModId?.let { modsById[it] }
                                    AssistantMessageBlock(
                                        message = msg,
                                        linkedMod = linkedMod,
                                        onCycleSkin = onCycleModSkin,
                                        onSaveMcAddon = { mod ->
                                            pendingSaveMod = mod
                                            val safeFile = mod.entityIdentifier
                                                .substringAfter(":")
                                                .ifBlank { "roxy_mod" } + ".mcaddon"
                                            saveDocumentLauncher.launch(safeFile)
                                        },
                                        onOpenInMinecraft = onShareMcAddon,
                                        onCustomizeInStudio = {
                                            onNavigateToDestination(RoxyDestination.MINECRAFT_MODS)
                                        }
                                    )
                                }
                            }

                            if (isGeneratingResponse) {
                                item(key = "generating_skeleton_turn") {
                                    ChatTurnSkeletonCard(reduceMotion = reduceMotion)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom ChatGPT-Style Composer Bar with Slash Commands
            ChatGptBottomComposer(
                text = composerText,
                isGenerating = isGeneratingResponse,
                onTextChange = { composerText = it },
                onQuickChipClick = { quickPrompt ->
                    onSendChatPrompt(quickPrompt)
                },
                onSend = {
                    val trimmed = composerText.trim()
                    if (trimmed.isNotEmpty()) {
                        composerText = ""
                        onSendChatPrompt(trimmed)
                    }
                }
            )
        }
    }

    if (showModelPickerDialog) {
        ModelAndVersionPickerDialog(
            currentModel = activeThread?.selectedModel ?: "RoxyGPT-4o • Mod Engine",
            currentVersion = activeThread?.bedrockVersion ?: "1.21.70+",
            onDismiss = { showModelPickerDialog = false },
            onConfirm = { model, version ->
                showModelPickerDialog = false
                onUpdateThreadModelAndVersion(model, version)
            }
        )
    }
}

@Composable
private fun ChatGptTopBar(
    modelLabel: String,
    bedrockVersion: String,
    onOpenDrawer: () -> Unit,
    onOpenModelSelector: () -> Unit,
    onNewChatClick: () -> Unit,
    onRefreshSkeletonClick: () -> Unit
) {
    Surface(
        color = RoxyObsidian,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("open_chat_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Abrir historial y menú lateral",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Center Model & Bedrock Version Pill (ChatGPT style)
            Surface(
                color = RoxyCardSurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenModelSelector)
                    .testTag("chat_model_selector_pill")
            ) {
                Row(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$modelLabel • MC $bedrockVersion",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Cambiar modelo o versión",
                        tint = RoxyTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefreshSkeletonClick,
                    modifier = Modifier.testTag("chat_refresh_skeleton_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Ver animación Skeleton",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier.testTag("new_chat_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nuevo chat",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatGptZeroStateWelcome(
    bedrockVersion: String,
    onPromptStarterClick: (String) -> Unit
) {
    val starters = listOf(
        Triple(
            "✨ Compañero NPC Leal / Anime",
            "Sigue, conversa y te protege en todo el mundo",
            "Hazme un mod para Minecraft Bedrock $bedrockVersion en Android de un compañero NPC estilo femboy anime muy leal y afectuoso que me siga a todos lados, acepte flores o diamantes y me defienda de enemigos."
        ),
        Triple(
            "💖 Compañera Cariñosa & Guardiana",
            "Diálogos personalizados + Regalos + Spawn natural",
            "Crea un mod .mcaddon para Minecraft Bedrock $bedrockVersion de una compañera cariñosa y protectora que camine conmigo, tenga frases dulces al interactuar y acepte manzanas doradas o flores."
        ),
        Triple(
            "⚔️ Arma Mítica + Invocación",
            "Receta de crafteo + 24 ATQ + Guardián",
            "Crea un mod de una espada mítica brillante para Minecraft Bedrock $bedrockVersion con receta en mesa de crafteo, 24 de daño y un guardián invocado."
        ),
        Triple(
            "🐉 Boss o Criatura Personalizada",
            "Atributos, botín y skin 64x64 HD",
            "Hazme un mod para Minecraft Bedrock $bedrockVersion de una criatura guardiana épica domesticable con diamantes que tenga 240 de vida y me acompañe."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "¿Qué mod o proyecto creamos hoy?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Pídeme cualquier mod para Minecraft Bedrock ($bedrockVersion) y compilaré los 12 archivos del paquete .mcaddon listos para instalar en Android.",
            style = MaterialTheme.typography.bodyMedium,
            color = RoxyTextSecondary
        )

        Spacer(modifier = Modifier.height(22.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            starters.forEachIndexed { idx, (title, subtitle, fullPrompt) ->
                Surface(
                    color = RoxyCardSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPromptStarterClick(fullPrompt) }
                        .testTag("chat_starter_card_$idx")
                ) {
                    Row(
                        modifier = Modifier
                            .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoxyTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessageEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = RoxyCardElevated,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 4.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .border(
                        1.dp,
                        RoxyBorderSubtle,
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun AssistantMessageBlock(
    message: ChatMessageEntity,
    linkedMod: MinecraftModEntity?,
    onCycleSkin: (MinecraftModEntity) -> Unit,
    onSaveMcAddon: (MinecraftModEntity) -> Unit,
    onOpenInMinecraft: (MinecraftModEntity) -> Unit,
    onCustomizeInStudio: () -> Unit
) {
    val context = LocalContext.current
    var showCodeSnippet by rememberSaveable { mutableStateOf(false) }
    var activeDialogueIndex by rememberSaveable { mutableIntStateOf(0) }
    var giftHeartCount by rememberSaveable { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assistant_message_${message.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NexusRoxy IA • Mod & Agent Studio",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { copyToClipboardSafe(context, "Respuesta IA", message.content) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar respuesta",
                    tint = RoxyTextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Embedded Interactive .mcaddon Artifact Card inside the ChatGPT conversation
        if (linkedMod != null) {
            val allLines = remember(linkedMod.dialogueLines) {
                linkedMod.dialogueLines.lines().filter { it.isNotBlank() }
                    .ifEmpty { listOf("¡Siempre iré contigo a donde vayas!") }
            }
            val currentQuote = allLines[activeDialogueIndex % allLines.size]

            Surface(
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chat_embedded_mod_card_${linkedMod.id}")
            ) {
                Column(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FullBodyMinecraftSkinPreview(
                            primaryHex = linkedMod.primaryColorHex,
                            secondaryHex = linkedMod.secondaryColorHex,
                            modifier = Modifier.size(width = 46.dp, height = 62.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = linkedMod.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${linkedMod.entityIdentifier} • Bedrock ${linkedMod.bedrockVersion}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "❤️ ${linkedMod.maxHealth} HP • ⚔️ ${linkedMod.attackDamage} ATQ • 🎁 ${linkedMod.tameItem.substringAfter(":")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoxyTextSecondary
                            )
                            Text(
                                text = "Incluye Spawn Natural + Huevo + /summon ${linkedMod.entityIdentifier}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoxyEmerald
                            )
                        }
                        IconButton(
                            onClick = { onCycleSkin(linkedMod) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Cambiar color de skin",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Interactive In-Chat NPC Dialogue & Gift Simulator
                    Surface(
                        color = RoxyCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                activeDialogueIndex = (activeDialogueIndex + 1) % allLines.size
                                giftHeartCount = (giftHeartCount % 5) + 1
                            }
                            .testTag("simulate_npc_dialogue_${linkedMod.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "💬 \"$currentQuote\"",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Toca aquí para darle ${linkedMod.tameItem.substringAfter(":")} y escuchar su siguiente frase (${"❤️".repeat(giftHeartCount)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoxyRose
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = RoxyRose,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 1-Tap Save .mcaddon & Open in Minecraft buttons right inside chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSaveMcAddon(linkedMod) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_save_mcaddon_${linkedMod.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Guardar .mcaddon",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onOpenInMinecraft(linkedMod) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoxyEmerald,
                                contentColor = Color(0xFF002619)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_open_mc_${linkedMod.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Abrir en Minecraft",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCodeSnippet = !showCodeSnippet }) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showCodeSnippet) "Ocultar JSON" else "Ver JSON Bedrock",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        TextButton(
                            onClick = {
                                copyToClipboardSafe(
                                    context,
                                    "Summon Command",
                                    "/summon ${linkedMod.entityIdentifier} ~ ~ ~"
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copiar /summon",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        TextButton(onClick = onCustomizeInStudio) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Estudio",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    AnimatedVisibility(visible = showCodeSnippet && message.codeSnippet.isNotBlank()) {
                        Surface(
                            color = RoxyObsidian,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = message.codeSnippet,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatGptBottomComposer(
    text: String,
    isGenerating: Boolean,
    onTextChange: (String) -> Unit,
    onQuickChipClick: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = RoxyCardSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Slash-Command & Quick Preset Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickChipClick("/femboy Hazme un mod de un compañero NPC estilo femboy anime muy leal y afectuoso que me siga a todos lados, acepte flores y me proteja en Minecraft Bedrock")
                    },
                    label = { Text("/femboy • Compañero Leal", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("slash_cmd_femboy")
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickChipClick("/cariñosa Hazme un mod de una compañera cariñosa y guardiana con diálogos dulces que acepte flores y diamantes en Minecraft Bedrock")
                    },
                    label = { Text("/cariñosa • Compañera", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("slash_cmd_carinosa")
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickChipClick("/arma Hazme un mod de una katana mítica con receta de crafteo, 26 de daño y partículas para Minecraft Bedrock")
                    },
                    label = { Text("/arma • Katana Mítica", style = MaterialTheme.typography.labelMedium) }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickChipClick("/boss Hazme un mod de un guardián colosal domesticable con 300 HP y tabla de botín épica")
                    },
                    label = { Text("/boss • Guardián Épico", style = MaterialTheme.typography.labelMedium) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chatgpt_composer_input"),
                    placeholder = {
                        Text(
                            text = "Pide cualquier mod o usa /femboy, /cariñosa, /arma…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RoxyTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                Button(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isGenerating,
                    shape = CircleShape,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chatgpt_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Enviar mensaje",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatGptDrawerContent(
    threads: List<ChatThreadEntity>,
    activeThreadId: Long?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onSelectThread: (Long) -> Unit,
    onDeleteThread: (Long) -> Unit,
    onNavigateToSection: (RoxyDestination) -> Unit
) {
    val filteredThreads = threads.filter {
        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Button(
            onClick = onNewChatClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_new_chat_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Chat / Crear Mod", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar en historial…", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "CONVERSACIONES Y MODS",
            style = MaterialTheme.typography.labelSmall,
            color = RoxyTextMuted
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredThreads, key = { it.id }) { thread ->
                val isSelected = thread.id == activeThreadId
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectThread(thread.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else RoxyTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = thread.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "MC ${thread.bedrockVersion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoxyTextMuted
                                )
                            }
                        }
                        if (threads.size > 1) {
                            IconButton(
                                onClick = { onDeleteThread(thread.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar conversación",
                                    tint = RoxyTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = RoxyBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "HERRAMIENTAS DEL ESTUDIO",
            style = MaterialTheme.typography.labelSmall,
            color = RoxyTextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
            onClick = { onNavigateToSection(RoxyDestination.MINECRAFT_MODS) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Biblioteca y Editor Visual de Mods")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onNavigateToSection(RoxyDestination.HOME_AGENTS) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agentes")
            }
            OutlinedButton(
                onClick = { onNavigateToSection(RoxyDestination.EXPLORE_HUB) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Explorar")
            }
            OutlinedButton(
                onClick = { onNavigateToSection(RoxyDestination.PROFILE_SETTINGS) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("API")
            }
        }
    }
}

@Composable
private fun ModelAndVersionPickerDialog(
    currentModel: String,
    currentVersion: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedModel by rememberSaveable { mutableStateOf(currentModel) }
    var selectedVersion by rememberSaveable { mutableStateOf(currentVersion) }

    val models = listOf(
        "RoxyGPT-4o • Mod Engine",
        "Claude 3.7 Sonnet • Roxy",
        "Gemini 2.5 Pro • Creator"
    )
    val versions = listOf("1.21.70+", "1.21.50", "1.21.0", "1.20.80")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoxyCardSurface,
        title = {
            Text("Seleccionar Modelo IA y Versión de Minecraft", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Modelo de Generación:", style = MaterialTheme.typography.labelLarge)
                models.forEach { mdl ->
                    FilterChip(
                        selected = selectedModel == mdl,
                        onClick = { selectedModel = mdl },
                        label = { Text(mdl) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Versión de Minecraft Bedrock:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    versions.forEach { ver ->
                        FilterChip(
                            selected = selectedVersion == ver,
                            onClick = { selectedVersion = ver },
                            label = { Text("MC $ver") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedModel, selectedVersion) }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
