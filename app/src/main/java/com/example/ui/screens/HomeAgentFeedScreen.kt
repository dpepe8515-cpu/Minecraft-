package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AgentTaskEntity
import com.example.ui.components.AgentFeedSkeletonList
import com.example.ui.theme.RoxyAmber
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardElevated
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.RoxyEmerald
import com.example.ui.theme.RoxyObsidian
import com.example.ui.theme.RoxyRose
import com.example.ui.theme.RoxyTextMuted
import com.example.ui.theme.RoxyTextSecondary
import com.example.viewmodel.FeedUiState

@Composable
fun HomeAgentFeedScreen(
    feedState: FeedUiState<List<AgentTaskEntity>>,
    searchQuery: String,
    selectedStatus: String,
    onlyBookmarked: Boolean,
    reduceMotion: Boolean,
    defaultModel: String,
    onSearchQueryChange: (String) -> Unit,
    onStatusSelect: (String) -> Unit,
    onToggleBookmarkedFilter: () -> Unit,
    onResetFilters: () -> Unit,
    onRefreshFeed: (Boolean) -> Unit,
    onSelectTask: (Long) -> Unit,
    onToggleTaskBookmark: (AgentTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onCreateNewTask: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact Hero & Action Header
        HomeHeroBanner(
            onNewTaskClick = { showCreateDialog = true },
            onRefreshSkeletonClick = { onRefreshFeed(false) }
        )

        // Search and Filter Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_input"),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_agents_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoxyTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar agentes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_home_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val statuses = listOf(
                "ALL" to "Todos",
                "RUNNING" to "En Ejecución",
                "REVIEW" to "En Revisión",
                "COMPLETED" to "Completados",
                "ERROR" to "Con Error"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statuses.forEach { (key, label) ->
                    val selected = selectedStatus == key
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusSelect(key) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("status_chip_$key")
                    )
                }

                FilterChip(
                    selected = onlyBookmarked,
                    onClick = onToggleBookmarkedFilter,
                    label = { Text("Guardados", style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (onlyBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Filtrar guardados",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("bookmark_filter_chip")
                )

                TextButton(
                    onClick = { onRefreshFeed(true) },
                    modifier = Modifier.testTag("simulate_error_button")
                ) {
                    Text(
                        text = "Probar Error",
                        style = MaterialTheme.typography.labelMedium,
                        color = RoxyTextSecondary
                    )
                }
            }
        }

        // Main Content Feed with Skeleton Shimmer replacement (zero static loading spinners)
        Crossfade(
            targetState = feedState,
            animationSpec = tween(durationMillis = 260),
            label = "home_feed_crossfade",
            modifier = Modifier.weight(1f)
        ) { state ->
            when (state) {
                is FeedUiState.Loading -> {
                    AgentFeedSkeletonList(reduceMotion = reduceMotion)
                }

                is FeedUiState.Error -> {
                    FeedErrorStateCard(
                        message = state.message,
                        onRetry = { onRefreshFeed(false) }
                    )
                }

                is FeedUiState.Success -> {
                    if (state.items.isEmpty()) {
                        EmptyFeedPlaceholder(onResetFilters = onResetFilters)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .testTag("agent_feed_list"),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(key = "telemetry_summary") {
                                TelemetrySummaryRow(tasks = state.items)
                            }

                            items(
                                items = state.items,
                                key = { it.id },
                                contentType = { "agent_task_card" }
                            ) { task ->
                                ParallelAgentTaskCard(
                                    task = task,
                                    onOpenTerminal = { onSelectTask(task.id) },
                                    onToggleBookmark = { onToggleTaskBookmark(task) },
                                    onDelete = { onDeleteTask(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        NewAgentTaskDialog(
            defaultModel = defaultModel,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, repo, model, prompt ->
                showCreateDialog = false
                onCreateNewTask(title, repo, model, prompt)
            }
        )
    }
}

@Composable
private fun HomeHeroBanner(
    onNewTaskClick: () -> Unit,
    onRefreshSkeletonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_roxy_hero),
            contentDescription = "Banner de espacio de trabajo Roxy",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            RoxyObsidian.copy(alpha = 0.92f),
                            RoxyObsidian.copy(alpha = 0.78f),
                            RoxyObsidian.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROXY.GG PARALLEL HARNESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.hero_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.hero_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RoxyTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onNewTaskClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("new_agent_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear agente",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.new_agent_task),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onRefreshSkeletonClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("refresh_skeleton_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar feed con skeleton",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sincronizar (Skeleton)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetrySummaryRow(tasks: List<AgentTaskEntity>) {
    val activeCount = tasks.count { it.status == "RUNNING" }
    val avgLatency = tasks.map { it.latencyMs }.average().toInt().coerceAtLeast(180)
    val totalFiles = tasks.sumOf { it.filesChanged }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            label = "WORKTREES",
            value = "$activeCount activos / ${tasks.size}",
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "LATENCIA API",
            value = "${avgLatency}ms TTFT",
            accentColor = RoxyEmerald,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "ARCHIVOS DIFF",
            value = "$totalFiles mod.",
            accentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = RoxyCardSurface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RoxyTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ParallelAgentTaskCard(
    task: AgentTaskEntity,
    onOpenTerminal: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusLabel) = when (task.status) {
        "RUNNING" -> MaterialTheme.colorScheme.primary to "EN EJECUCIÓN"
        "COMPLETED" -> RoxyEmerald to "COMPLETADO"
        "REVIEW" -> RoxyAmber to "REVISAR DIFF"
        else -> RoxyRose to "ERROR / TIMEOUT"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenTerminal)
            .testTag("agent_task_card_${task.id}"),
        color = RoxyCardSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Top bar: Status badge + Repo + Worktree pill + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.repository,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = RoxyCardElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = task.worktreeBranch,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_task_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Guardar agente",
                        tint = if (task.isBookmarked) MaterialTheme.colorScheme.primary else RoxyTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.lastStepSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = RoxyTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
                Text(
                    text = "${task.progressPercent}% • ${task.filesChanged} archivos",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoxyTextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { task.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = RoxyCardElevated
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer telemetry & Terminal CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = RoxyTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.modelProvider.substringAfterLast("/").trim(),
                            style = MaterialTheme.typography.labelMedium,
                            color = RoxyTextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = RoxyTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.latencyMs}ms",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoxyTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar tarea",
                            tint = RoxyTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable(onClick = onOpenTerminal)
                            .testTag("open_terminal_${task.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Terminal",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFeedPlaceholder(
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("empty_feed_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.empty_feed_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.empty_feed_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = RoxyTextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onResetFilters,
            modifier = Modifier.testTag("reset_filters_button")
        ) {
            Text(text = stringResource(R.string.reset_filters))
        }
    }
}

@Composable
fun FeedErrorStateCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("feed_error_state"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = RoxyCardSurface,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, RoxyRose.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error de sincronización",
                    tint = RoxyRose,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Error de Sincronización",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RoxyTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("retry_feed_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.retry_action))
                }
            }
        }
    }
}

@Composable
private fun NewAgentTaskDialog(
    defaultModel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var repository by rememberSaveable { mutableStateOf("roxy-gg/roxy") }
    var modelName by rememberSaveable { mutableStateOf(defaultModel) }
    var prompt by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoxyCardSurface,
        title = {
            Text(
                text = "Lanzar Agente Paralelo",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Cada tarea recibe su propio git worktree aislado al estilo Roxy.gg para no interferir con otras ramas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RoxyTextSecondary
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea") },
                    placeholder = { Text("Ej. Optimizar animaciones Skeleton") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_task_title_input")
                )
                OutlinedTextField(
                    value = repository,
                    onValueChange = { repository = it },
                    label = { Text("Repositorio objetivo") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_task_repo_input")
                )
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Modelo (OpenAI / Roxy Gateway)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Instrucción para el agente") },
                    placeholder = { Text("Describe los cambios y pruebas a ejecutar…") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_task_prompt_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, repository, modelName, prompt) },
                modifier = Modifier.testTag("confirm_create_task_button")
            ) {
                Text("Crear Worktree y Ejecutar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
