package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AgentLogEntity
import com.example.data.AgentTaskEntity
import com.example.ui.components.WorkspaceTerminalSkeleton
import com.example.ui.theme.RoxyAmber
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardElevated
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.RoxyEmerald
import com.example.ui.theme.RoxyPurple
import com.example.ui.theme.RoxyTextMuted
import com.example.ui.theme.RoxyTextSecondary
import com.example.viewmodel.FeedUiState

@Composable
fun WorkspaceTerminalScreen(
    tasks: List<AgentTaskEntity>,
    selectedTaskId: Long,
    logsState: FeedUiState<List<AgentLogEntity>>,
    isRunningPrompt: Boolean,
    reduceMotion: Boolean,
    onSelectTask: (Long) -> Unit,
    onRefreshLogs: () -> Unit,
    onSendPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by rememberSaveable { mutableStateOf("") }
    val activeTask = tasks.find { it.id == selectedTaskId } ?: tasks.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Selector of Parallel Agent Worktrees
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Terminal de Worktree Aislado",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = onRefreshLogs,
                    modifier = Modifier.testTag("refresh_workspace_skeleton_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sincronizar logs con skeleton",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal selector for active parallel agent sessions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    val isSelected = task.id == activeTask?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectTask(task.id) },
                        label = {
                            Text(
                                text = "${task.worktreeBranch} (${task.progressPercent}%)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("workspace_task_chip_${task.id}")
                    )
                }
            }
        }

        // Main Terminal Log Feed with Skeleton Shimmer replacement
        Crossfade(
            targetState = logsState,
            animationSpec = tween(durationMillis = 250),
            label = "workspace_logs_crossfade",
            modifier = Modifier.weight(1f)
        ) { state ->
            when (state) {
                is FeedUiState.Loading -> {
                    WorkspaceTerminalSkeleton(reduceMotion = reduceMotion)
                }

                is FeedUiState.Error -> {
                    FeedErrorStateCard(
                        message = state.message,
                        onRetry = onRefreshLogs
                    )
                }

                is FeedUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("workspace_logs_list"),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (activeTask != null) {
                            item(key = "active_task_banner") {
                                ActiveWorktreeSummaryBanner(task = activeTask)
                            }
                        }

                        items(
                            items = state.items,
                            key = { it.id },
                            contentType = { "agent_log_step" }
                        ) { logItem ->
                            AgentStepLogCard(log = logItem)
                        }
                    }
                }
            }
        }

        // Bottom Interactive Prompt Input Bar
        Surface(
            color = RoxyCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                if (isRunningPrompt) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = RoxyCardElevated
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_prompt_input"),
                        placeholder = {
                            Text(
                                text = "Instruir al agente en ${activeTask?.worktreeBranch ?: "wt/main"}…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoxyTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val toSend = promptInput.trim()
                            if (toSend.isNotEmpty()) {
                                promptInput = ""
                                onSendPrompt(toSend)
                            }
                        },
                        enabled = promptInput.isNotBlank() && !isRunningPrompt,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("terminal_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar instrucción al agente",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveWorktreeSummaryBanner(task: AgentTaskEntity) {
    Surface(
        color = RoxyCardElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Repo: ${task.repository}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tokens: ${task.tokensUsed}",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoxyTextSecondary
                )
                Text(
                    text = "Diffs: ${task.filesChanged} archivos",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoxyEmerald
                )
            }
        }
    }
}

@Composable
private fun AgentStepLogCard(log: AgentLogEntity) {
    val badgeColor = when (log.stepType) {
        "WORKTREE" -> MaterialTheme.colorScheme.primary
        "PLAN" -> RoxyPurple
        "TOOL_CALL" -> RoxyAmber
        "DIFF" -> RoxyEmerald
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = RoxyCardSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = log.stepType,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = log.detail,
                style = MaterialTheme.typography.labelMedium,
                color = RoxyTextSecondary
            )
        }
    }
}
