package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.SkeletonBaseDark
import com.example.ui.theme.SkeletonCyanTint
import com.example.ui.theme.SkeletonHighlightDark

/**
 * Shared directional shimmer brush that replaces static CircularProgressIndicators
 * across all main content feeds to improve perceived loading performance and prevent layout shift.
 */
@Composable
fun rememberRoxyShimmerBrush(reduceMotion: Boolean = false): Brush {
    val transition = rememberInfiniteTransition(label = "roxy_skeleton_transition")
    if (reduceMotion) {
        val alpha by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "skeleton_pulse_alpha"
        )
        return SolidColor(SkeletonHighlightDark.copy(alpha = alpha))
    }

    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            SkeletonBaseDark,
            SkeletonHighlightDark,
            SkeletonCyanTint,
            SkeletonHighlightDark,
            SkeletonBaseDark
        ),
        start = Offset(translateAnim - 320f, translateAnim - 320f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun SkeletonBlock(
    brush: Brush,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    cornerRadius: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * Skeleton Screen for the Home Parallel Agent Feed.
 * Mirrors the exact layout of summary metric tiles and parallel agent worktree cards.
 */
@Composable
fun AgentFeedSkeletonList(
    reduceMotion: Boolean = false,
    cardCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val brush = rememberRoxyShimmerBrush(reduceMotion = reduceMotion)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("agent_feed_skeleton")
            .semantics { contentDescription = "Cargando feed de agentes con animación skeleton" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Telemetry summary strip skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(3) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = RoxyCardSurface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            SkeletonBlock(brush = brush, modifier = Modifier.width(48.dp), height = 10.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            SkeletonBlock(brush = brush, modifier = Modifier.width(64.dp), height = 20.dp)
                        }
                    }
                }
            }
        }

        items(cardCount) { index ->
            AgentTaskSkeletonCard(brush = brush, index = index)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AgentTaskSkeletonCard(
    brush: Brush,
    index: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_skeleton_card_$index"),
        color = RoxyCardSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Top row: status dot + repo pill + worktree branch badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.width(110.dp), height = 14.dp)
                }
                SkeletonBlock(
                    brush = brush,
                    modifier = Modifier.width(96.dp),
                    height = 20.dp,
                    cornerRadius = 10.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task title bar
            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.86f), height = 18.dp)
            Spacer(modifier = Modifier.height(8.dp))
            // Last log snippet 2-line placeholder
            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.95f), height = 13.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.65f), height = 13.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar track skeleton
            SkeletonBlock(
                brush = brush,
                modifier = Modifier.fillMaxWidth(),
                height = 6.dp,
                cornerRadius = 3.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer metrics: model chip, tokens, latency, action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(brush = brush, modifier = Modifier.width(84.dp), height = 16.dp)
                    SkeletonBlock(brush = brush, modifier = Modifier.width(62.dp), height = 16.dp)
                }
                SkeletonBlock(
                    brush = brush,
                    modifier = Modifier.width(72.dp),
                    height = 28.dp,
                    cornerRadius = 8.dp
                )
            }
        }
    }
}

/**
 * Skeleton Screen for the Explore & Search Catalog Feed (Models, MCP Skills, Templates, GitHub Repos).
 */
@Composable
fun ExploreCatalogSkeletonList(
    reduceMotion: Boolean = false,
    cardCount: Int = 5,
    modifier: Modifier = Modifier
) {
    val brush = rememberRoxyShimmerBrush(reduceMotion = reduceMotion)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("explore_feed_skeleton")
            .semantics { contentDescription = "Cargando catálogo con animación skeleton" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cardCount) { index ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_skeleton_card_$index"),
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.72f), height = 17.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.45f), height = 12.dp)
                        }
                        SkeletonBlock(
                            brush = brush,
                            modifier = Modifier.width(68.dp),
                            height = 22.dp,
                            cornerRadius = 11.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.94f), height = 13.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.76f), height = 13.dp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBlock(brush = brush, modifier = Modifier.width(130.dp), height = 15.dp)
                        SkeletonBlock(
                            brush = brush,
                            modifier = Modifier.width(92.dp),
                            height = 32.dp,
                            cornerRadius = 10.dp
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Skeleton Screen for the Workspace & Live Agent Terminal Log Feed.
 */
@Composable
fun WorkspaceTerminalSkeleton(
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val brush = rememberRoxyShimmerBrush(reduceMotion = reduceMotion)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("workspace_feed_skeleton")
            .semantics { contentDescription = "Cargando registros de sesión de agente con skeleton" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header worktree banner skeleton
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RoxyCardSurface,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.6f), height = 16.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkeletonBlock(brush = brush, modifier = Modifier.width(90.dp), height = 14.dp)
                    SkeletonBlock(brush = brush, modifier = Modifier.width(80.dp), height = 14.dp)
                    SkeletonBlock(brush = brush, modifier = Modifier.width(70.dp), height = 14.dp)
                }
            }
        }

        // Terminal log steps skeleton
        repeat(4) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RoxyCardSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeletonBlock(
                            brush = brush,
                            modifier = Modifier.width(74.dp),
                            height = 18.dp,
                            cornerRadius = 6.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.65f), height = 14.dp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.92f), height = 12.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.55f), height = 12.dp)
                }
            }
        }

        Text(
            text = "Sincronizando árbol de trabajo aislado y telemetría de agente…",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Skeleton Screen for the Minecraft Bedrock AI Mod Studio Feed.
 */
@Composable
fun ModStudioSkeletonList(
    reduceMotion: Boolean = false,
    cardCount: Int = 3,
    modifier: Modifier = Modifier
) {
    val brush = rememberRoxyShimmerBrush(reduceMotion = reduceMotion)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("mod_studio_skeleton")
            .semantics { contentDescription = "Cargando estudio de mods de Minecraft con animación skeleton" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cardCount) { idx ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mod_skeleton_card_$idx"),
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.75f), height = 18.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.5f), height = 13.dp)
                        }
                        SkeletonBlock(
                            brush = brush,
                            modifier = Modifier.width(64.dp),
                            height = 22.dp,
                            cornerRadius = 8.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.95f), height = 13.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.7f), height = 13.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SkeletonBlock(brush = brush, modifier = Modifier.width(105.dp), height = 34.dp, cornerRadius = 10.dp)
                        SkeletonBlock(brush = brush, modifier = Modifier.width(135.dp), height = 34.dp, cornerRadius = 10.dp)
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Skeleton Shimmer placeholder rendered inside the ChatGPT-style conversation stream
 * while the AI synthesizes a response and compiles an embedded `.mcaddon` artifact card.
 */
@Composable
fun ChatTurnSkeletonCard(
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val brush = rememberRoxyShimmerBrush(reduceMotion = reduceMotion)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_turn_skeleton")
            .semantics { contentDescription = "Generando respuesta y compilando mod con animación skeleton" },
        color = RoxyCardSurface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(10.dp))
                SkeletonBlock(brush = brush, modifier = Modifier.width(150.dp), height = 14.dp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.92f), height = 13.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.84f), height = 13.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.60f), height = 13.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Embedded .mcaddon artifact skeleton preview
            Surface(
                color = RoxyCardSurface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.7f), height = 16.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            SkeletonBlock(brush = brush, modifier = Modifier.fillMaxWidth(0.45f), height = 12.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBlock(
                            brush = brush,
                            modifier = Modifier.weight(1f),
                            height = 36.dp,
                            cornerRadius = 10.dp
                        )
                        SkeletonBlock(
                            brush = brush,
                            modifier = Modifier.weight(1f),
                            height = 36.dp,
                            cornerRadius = 10.dp
                        )
                    }
                }
            }
        }
    }
}


