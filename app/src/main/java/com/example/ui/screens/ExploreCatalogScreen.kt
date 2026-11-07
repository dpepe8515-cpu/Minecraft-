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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CatalogItemEntity
import com.example.ui.components.ExploreCatalogSkeletonList
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardElevated
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.RoxyEmerald
import com.example.ui.theme.RoxyPurple
import com.example.ui.theme.RoxyTextMuted
import com.example.ui.theme.RoxyTextSecondary
import com.example.viewmodel.FeedUiState

@Composable
fun ExploreCatalogScreen(
    feedState: FeedUiState<List<CatalogItemEntity>>,
    searchQuery: String,
    selectedCategory: String,
    reduceMotion: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onRefreshCatalog: () -> Unit,
    onSearchLiveGitHub: (String) -> Unit,
    onToggleInstallItem: (CatalogItemEntity) -> Unit,
    onLaunchAgentWithItem: (CatalogItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header & Live GitHub API Sync Bar
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ecosistema Roxy & MCP Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Modelos BYOM, conectores Sirup MCP y búsqueda en vivo en GitHub",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoxyTextSecondary
                    )
                }
                IconButton(
                    onClick = onRefreshCatalog,
                    modifier = Modifier.testTag("refresh_explore_skeleton_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar catálogo con skeleton",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("explore_search_input"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_catalog_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RoxyTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar en catálogo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
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

                Button(
                    onClick = { onSearchLiveGitHub(searchQuery) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                    modifier = Modifier.testTag("github_live_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Buscar en GitHub API",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GitHub API",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val categories = listOf(
                "ALL" to "Todo el Hub",
                "MODEL" to "Modelos IA",
                "MCP_SKILL" to "Skills MCP",
                "TEMPLATE" to "Plantillas",
                "GITHUB_REPO" to "Repos GitHub"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catKey, label) ->
                    val selected = selectedCategory == catKey
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelect(catKey) },
                        label = {
                            Text(text = label, style = MaterialTheme.typography.labelLarge)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("catalog_category_$catKey")
                    )
                }
            }
        }

        // Main Catalog Content Feed with Skeleton Shimmer replacement
        Crossfade(
            targetState = feedState,
            animationSpec = tween(durationMillis = 260),
            label = "explore_feed_crossfade",
            modifier = Modifier.weight(1f)
        ) { state ->
            when (state) {
                is FeedUiState.Loading -> {
                    ExploreCatalogSkeletonList(reduceMotion = reduceMotion)
                }

                is FeedUiState.Error -> {
                    FeedErrorStateCard(
                        message = state.message,
                        onRetry = onRefreshCatalog
                    )
                }

                is FeedUiState.Success -> {
                    if (state.items.isEmpty()) {
                        EmptyFeedPlaceholder(
                            onResetFilters = {
                                onSearchQueryChange("")
                                onCategorySelect("ALL")
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .testTag("explore_catalog_list"),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.items,
                                key = { it.id },
                                contentType = { "catalog_item_card" }
                            ) { item ->
                                CatalogItemCard(
                                    item = item,
                                    onToggleInstalled = { onToggleInstallItem(item) },
                                    onUseInAgent = { onLaunchAgentWithItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogItemCard(
    item: CatalogItemEntity,
    onToggleInstalled: () -> Unit,
    onUseInAgent: () -> Unit
) {
    val (categoryIcon, accentColor) = when (item.category) {
        "MODEL" -> Icons.Default.Memory to MaterialTheme.colorScheme.primary
        "MCP_SKILL" -> Icons.Default.Extension to RoxyPurple
        "TEMPLATE" -> Icons.Default.AutoAwesome to RoxyEmerald
        else -> Icons.Default.Hub to MaterialTheme.colorScheme.secondary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("catalog_card_${item.id}"),
        color = RoxyCardSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(16.dp))
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
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${item.provider} • ★ ${item.starCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = RoxyTextSecondary
                    )
                }
                Surface(
                    color = RoxyCardElevated,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = item.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = RoxyTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = RoxyCardElevated.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.endpointOrCommand,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.metricLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = RoxyTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onToggleInstalled,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("toggle_install_${item.id}")
                    ) {
                        if (item.isInstalled) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RoxyEmerald,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activo", style = MaterialTheme.typography.labelLarge, color = RoxyEmerald)
                        } else {
                            Text("Instalar", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Button(
                        onClick = onUseInAgent,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("use_in_agent_${item.id}")
                    ) {
                        Text("Usar en Agente", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
