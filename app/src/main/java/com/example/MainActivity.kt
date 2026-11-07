package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatGptStudioScreen
import com.example.ui.screens.ExploreCatalogScreen
import com.example.ui.screens.HomeAgentFeedScreen
import com.example.ui.screens.MinecraftModStudioScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.WorkspaceTerminalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoxyCardSurface
import com.example.viewmodel.RoxyDestination
import com.example.viewmodel.RoxyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val roxyViewModel: RoxyViewModel = viewModel()
            val userPrefs by roxyViewModel.userPreferences.collectAsStateWithLifecycle()

            MyApplicationTheme(accentIndex = userPrefs.accentThemeIndex) {
                NexusRoxyApp(viewModel = roxyViewModel)
            }
        }
    }
}

private data class NavTabItem(
    val destination: RoxyDestination,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun NexusRoxyApp(
    viewModel: RoxyViewModel,
    modifier: Modifier = Modifier
) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    val chatThreads by viewModel.chatThreadsFlow.collectAsStateWithLifecycle()
    val activeChatThreadId by viewModel.activeChatThreadId.collectAsStateWithLifecycle()
    val activeThreadMessages by viewModel.activeThreadMessages.collectAsStateWithLifecycle()
    val isLoadingChatThread by viewModel.isLoadingChatThread.collectAsStateWithLifecycle()
    val isGeneratingChatResponse by viewModel.isGeneratingChatResponse.collectAsStateWithLifecycle()
    val modsById by viewModel.modsByIdFlow.collectAsStateWithLifecycle()

    val homeFeedState by viewModel.homeFeedState.collectAsStateWithLifecycle()
    val agentSearchQuery by viewModel.agentSearchQuery.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val onlyBookmarked by viewModel.onlyBookmarked.collectAsStateWithLifecycle()

    val modStudioFeedState by viewModel.modStudioFeedState.collectAsStateWithLifecycle()
    val selectedModCategory by viewModel.selectedModCategory.collectAsStateWithLifecycle()
    val isGeneratingMod by viewModel.isGeneratingMod.collectAsStateWithLifecycle()

    val exploreFeedState by viewModel.exploreFeedState.collectAsStateWithLifecycle()
    val catalogSearchQuery by viewModel.catalogSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val allTasks by viewModel.allTasksFlow.collectAsStateWithLifecycle()
    val selectedTaskId by viewModel.selectedTaskId.collectAsStateWithLifecycle()
    val workspaceFeedState by viewModel.workspaceFeedState.collectAsStateWithLifecycle()
    val isRunningPrompt by viewModel.isRunningPrompt.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    // Ensure hardware back button returns to CHAT_GPT_STUDIO from secondary destinations
    BackHandler(enabled = currentDestination != RoxyDestination.CHAT_GPT_STUDIO) {
        viewModel.navigateTo(RoxyDestination.CHAT_GPT_STUDIO)
    }

    val navItems = listOf(
        NavTabItem(
            destination = RoxyDestination.CHAT_GPT_STUDIO,
            labelResId = R.string.nav_chat_ai,
            selectedIcon = Icons.Filled.ChatBubble,
            unselectedIcon = Icons.Outlined.ChatBubbleOutline,
            testTag = "nav_tab_chat_ai"
        ),
        NavTabItem(
            destination = RoxyDestination.MINECRAFT_MODS,
            labelResId = R.string.nav_mods_mc,
            selectedIcon = Icons.Filled.AutoAwesome,
            unselectedIcon = Icons.Outlined.AutoAwesome,
            testTag = "nav_tab_mods_mc"
        ),
        NavTabItem(
            destination = RoxyDestination.HOME_AGENTS,
            labelResId = R.string.nav_agents,
            selectedIcon = Icons.Filled.Hub,
            unselectedIcon = Icons.Outlined.Hub,
            testTag = "nav_tab_agents"
        ),
        NavTabItem(
            destination = RoxyDestination.EXPLORE_HUB,
            labelResId = R.string.nav_explore,
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore,
            testTag = "nav_tab_explore"
        ),
        NavTabItem(
            destination = RoxyDestination.PROFILE_SETTINGS,
            labelResId = R.string.nav_settings,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            testTag = "nav_tab_settings"
        )
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = RoxyCardSurface,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination == item.destination
                            val label = stringResource(item.labelResId)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(text = label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = RoxyCardSurface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            navItems.forEach { item ->
                                val selected = currentDestination == item.destination
                                val label = stringResource(item.labelResId)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(text = label) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        when (currentDestination) {
                            RoxyDestination.CHAT_GPT_STUDIO -> {
                                val activeThread = chatThreads.find { it.id == activeChatThreadId }
                                    ?: chatThreads.firstOrNull()
                                ChatGptStudioScreen(
                                    threads = chatThreads,
                                    activeThread = activeThread,
                                    messages = activeThreadMessages,
                                    modsById = modsById,
                                    isLoadingThread = isLoadingChatThread,
                                    isGeneratingResponse = isGeneratingChatResponse,
                                    reduceMotion = userPrefs.reduceMotion,
                                    onSelectThread = viewModel::selectChatThread,
                                    onCreateNewThread = viewModel::createNewChatThread,
                                    onDeleteThread = viewModel::deleteChatThread,
                                    onUpdateThreadModelAndVersion = viewModel::updateActiveThreadModelAndVersion,
                                    onSendChatPrompt = viewModel::sendChatGptPrompt,
                                    onCycleModSkin = viewModel::cycleModSkinPalette,
                                    onRefreshSkeletonPreview = viewModel::previewChatSkeletonAnimation,
                                    onSaveMcAddonToUri = viewModel::exportMcAddonToUri,
                                    onShareMcAddon = viewModel::shareMcAddonToMinecraft,
                                    onNavigateToDestination = viewModel::navigateTo
                                )
                            }

                            RoxyDestination.MINECRAFT_MODS -> {
                                MinecraftModStudioScreen(
                                    modsState = modStudioFeedState,
                                    selectedModCategory = selectedModCategory,
                                    isGeneratingMod = isGeneratingMod,
                                    reduceMotion = userPrefs.reduceMotion,
                                    onSelectCategory = viewModel::selectModStudioCategory,
                                    onRefreshMods = viewModel::refreshModStudioFeed,
                                    onGenerateAiMod = viewModel::generateAiMinecraftMod,
                                    onUpdateMod = viewModel::updateMinecraftMod,
                                    onCycleSkinPalette = viewModel::cycleModSkinPalette,
                                    onDeleteMod = viewModel::deleteMinecraftMod,
                                    onSaveMcAddonToUri = viewModel::exportMcAddonToUri,
                                    onShareMcAddon = viewModel::shareMcAddonToMinecraft
                                )
                            }

                            RoxyDestination.HOME_AGENTS -> {
                                HomeAgentFeedScreen(
                                    feedState = homeFeedState,
                                    searchQuery = agentSearchQuery,
                                    selectedStatus = selectedStatusFilter,
                                    onlyBookmarked = onlyBookmarked,
                                    reduceMotion = userPrefs.reduceMotion,
                                    defaultModel = userPrefs.defaultModel,
                                    onSearchQueryChange = viewModel::updateAgentSearchQuery,
                                    onStatusSelect = viewModel::selectStatusFilter,
                                    onToggleBookmarkedFilter = viewModel::toggleBookmarkedFilter,
                                    onResetFilters = viewModel::resetHomeFilters,
                                    onRefreshFeed = viewModel::refreshHomeFeed,
                                    onSelectTask = viewModel::selectTaskAndOpenTerminal,
                                    onToggleTaskBookmark = viewModel::toggleTaskBookmark,
                                    onDeleteTask = viewModel::deleteAgentTask,
                                    onCreateNewTask = viewModel::createNewAgentTask
                                )
                            }

                            RoxyDestination.EXPLORE_HUB -> {
                                ExploreCatalogScreen(
                                    feedState = exploreFeedState,
                                    searchQuery = catalogSearchQuery,
                                    selectedCategory = selectedCategory,
                                    reduceMotion = userPrefs.reduceMotion,
                                    onSearchQueryChange = viewModel::updateCatalogSearchQuery,
                                    onCategorySelect = viewModel::selectCatalogCategory,
                                    onRefreshCatalog = viewModel::refreshExploreCatalog,
                                    onSearchLiveGitHub = viewModel::searchLiveGitHubRepositories,
                                    onToggleInstallItem = viewModel::toggleCatalogItemInstalled,
                                    onLaunchAgentWithItem = { item ->
                                        viewModel.createNewAgentTask(
                                            title = "Integrar ${item.title}",
                                            repository = if (item.category == "GITHUB_REPO") item.title else "roxy-gg/roxy",
                                            modelName = userPrefs.defaultModel,
                                            initialPrompt = "Configurar y verificar ${item.title} (${item.endpointOrCommand}) en worktree aislado."
                                        )
                                    }
                                )
                            }

                            RoxyDestination.WORKSPACE_TERMINAL -> {
                                WorkspaceTerminalScreen(
                                    tasks = allTasks,
                                    selectedTaskId = selectedTaskId,
                                    logsState = workspaceFeedState,
                                    isRunningPrompt = isRunningPrompt,
                                    reduceMotion = userPrefs.reduceMotion,
                                    onSelectTask = viewModel::selectTaskAndOpenTerminal,
                                    onRefreshLogs = viewModel::refreshWorkspaceTerminal,
                                    onSendPrompt = viewModel::executeTerminalPrompt
                                )
                            }

                            RoxyDestination.PROFILE_SETTINGS -> {
                                ProfileSettingsScreen(
                                    preferences = userPrefs,
                                    onSaveProfileAndApi = viewModel::saveProfileAndApiSettings,
                                    onSelectAccentTheme = viewModel::updateAccentTheme,
                                    onUpdateSkeletonSettings = viewModel::updateSkeletonPreferences,
                                    onUpdateAutoIsolate = viewModel::updateAutoIsolateWorktrees
                                )
                            }
                        }
                    }
                }

                if (bannerMessage != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = RoxyCardSurface
                    ) {
                        Snackbar(
                            action = {
                                TextButton(onClick = viewModel::dismissBannerMessage) {
                                    Text("OK", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        ) {
                            Text(text = bannerMessage.orEmpty())
                        }
                    }
                }
            }
        }
    }
}
