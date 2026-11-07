package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BedrockAddonCompiler
import com.example.data.MinecraftModEntity
import com.example.ui.components.ModStudioSkeletonList
import com.example.ui.theme.RoxyAmber
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardElevated
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.RoxyEmerald
import com.example.ui.theme.RoxyPurple
import com.example.ui.theme.RoxyRose
import com.example.ui.theme.RoxyTextMuted
import com.example.ui.theme.RoxyTextSecondary
import com.example.viewmodel.FeedUiState

@Composable
fun MinecraftModStudioScreen(
    modsState: FeedUiState<List<MinecraftModEntity>>,
    selectedModCategory: String,
    isGeneratingMod: Boolean,
    reduceMotion: Boolean,
    onSelectCategory: (String) -> Unit,
    onRefreshMods: () -> Unit,
    onGenerateAiMod: (
        prompt: String,
        customName: String,
        category: String,
        bedrockVersion: String,
        primaryHex: String,
        secondaryHex: String
    ) -> Unit,
    onUpdateMod: (MinecraftModEntity) -> Unit,
    onCycleSkinPalette: (MinecraftModEntity) -> Unit,
    onDeleteMod: (Long) -> Unit,
    onSaveMcAddonToUri: (MinecraftModEntity, Uri) -> Unit,
    onShareMcAddon: (MinecraftModEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptText by rememberSaveable { mutableStateOf("") }
    var customModName by rememberSaveable { mutableStateOf("") }
    var newModCategory by rememberSaveable { mutableStateOf("NPC_COMPANION") }
    var selectedBedrockVer by rememberSaveable { mutableStateOf("1.21.70+") }
    var selectedPaletteIndex by rememberSaveable { mutableStateOf(0) }

    var editingMod by remember { mutableStateOf<MinecraftModEntity?>(null) }
    var inspectingJsonMod by remember { mutableStateOf<MinecraftModEntity?>(null) }
    var validatingPackageMod by remember { mutableStateOf<MinecraftModEntity?>(null) }
    var pendingSaveMod by remember { mutableStateOf<MinecraftModEntity?>(null) }

    val colorPalettes = listOf(
        Triple("Pastel Pink & Violet", "#F472B6", "#8B5CF6"),
        Triple("Cyber Cyan & Magenta", "#00F0FF", "#EC4899"),
        Triple("Emerald & Gold", "#10B981", "#F59E0B"),
        Triple("Crimson & Obsidian", "#F43F5E", "#1E293B")
    )

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val modToSave = pendingSaveMod
        if (uri != null && modToSave != null) {
            onSaveMcAddonToUri(modToSave, uri)
        }
        pendingSaveMod = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estudio IA de Mods Minecraft Bedrock",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Compilador .mcaddon de 11 archivos (BP + RP + Spawn Natural + Skins 64x64)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RoxyTextSecondary
                )
            }
            IconButton(
                onClick = onRefreshMods,
                modifier = Modifier.testTag("refresh_mods_skeleton_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar mods con animación skeleton",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Category Filter Row
        val categories = listOf(
            "ALL" to "Todos los Mods",
            "NPC_COMPANION" to "Compañeros NPC",
            "WEAPON_ITEM" to "Armas e Ítems",
            "MOB_BOSS" to "Criaturas y Bosses"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (key, label) ->
                FilterChip(
                    selected = selectedModCategory == key,
                    onClick = { onSelectCategory(key) },
                    label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("mod_category_chip_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Content Feed with Skeleton Shimmer replacement
        Crossfade(
            targetState = modsState,
            animationSpec = tween(durationMillis = 260),
            label = "mod_studio_crossfade",
            modifier = Modifier.weight(1f)
        ) { state ->
            when (state) {
                is FeedUiState.Loading -> {
                    ModStudioSkeletonList(reduceMotion = reduceMotion)
                }

                is FeedUiState.Error -> {
                    FeedErrorStateCard(
                        message = state.message,
                        onRetry = onRefreshMods
                    )
                }

                is FeedUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("mod_studio_list"),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "ai_mod_generator_card") {
                            AiModGeneratorCard(
                                promptText = promptText,
                                customModName = customModName,
                                selectedCategory = newModCategory,
                                selectedBedrockVer = selectedBedrockVer,
                                selectedPaletteIndex = selectedPaletteIndex,
                                palettes = colorPalettes,
                                isGenerating = isGeneratingMod,
                                onPromptChange = { promptText = it },
                                onNameChange = { customModName = it },
                                onCategoryChange = { newModCategory = it },
                                onBedrockVerChange = { selectedBedrockVer = it },
                                onPaletteChange = { selectedPaletteIndex = it },
                                onQuickPresetClick = { presetName, presetCat, presetPrompt, paletteIdx ->
                                    customModName = presetName
                                    newModCategory = presetCat
                                    promptText = presetPrompt
                                    selectedPaletteIndex = paletteIdx
                                },
                                onGenerateClick = {
                                    val (_, primHex, secHex) = colorPalettes[selectedPaletteIndex]
                                    onGenerateAiMod(
                                        promptText,
                                        customModName,
                                        newModCategory,
                                        selectedBedrockVer,
                                        primHex,
                                        secHex
                                    )
                                    promptText = ""
                                    customModName = ""
                                }
                            )
                        }

                        items(
                            items = state.items,
                            key = { it.id },
                            contentType = { "minecraft_mod_card" }
                        ) { mod ->
                            MinecraftModCard(
                                mod = mod,
                                onEditBehaviors = { editingMod = mod },
                                onInspectJson = { inspectingJsonMod = mod },
                                onValidatePackage = { validatingPackageMod = mod },
                                onCycleSkin = { onCycleSkinPalette(mod) },
                                onDelete = { onDeleteMod(mod.id) },
                                onSaveMcAddonFile = {
                                    pendingSaveMod = mod
                                    val safeFile = mod.entityIdentifier
                                        .substringAfter(":")
                                        .ifBlank { "roxy_mod" } + ".mcaddon"
                                    saveDocumentLauncher.launch(safeFile)
                                },
                                onShareToMinecraft = {
                                    onShareMcAddon(mod)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Behavior & Dialogue Editor Modal
    editingMod?.let { modToEdit ->
        ModBehaviorEditorDialog(
            mod = modToEdit,
            onDismiss = { editingMod = null },
            onSave = { updated ->
                editingMod = null
                onUpdateMod(updated)
            }
        )
    }

    // Bedrock JSON & Manifest Inspector Modal
    inspectingJsonMod?.let { modToInspect ->
        ModJsonInspectorDialog(
            mod = modToInspect,
            onDismiss = { inspectingJsonMod = null }
        )
    }

    // 11-File .mcaddon Package Diagnostic & Commands Validator Modal
    validatingPackageMod?.let { modToValidate ->
        AddonPackageValidatorDialog(
            mod = modToValidate,
            onDismiss = { validatingPackageMod = null }
        )
    }
}

@Composable
private fun AiModGeneratorCard(
    promptText: String,
    customModName: String,
    selectedCategory: String,
    selectedBedrockVer: String,
    selectedPaletteIndex: Int,
    palettes: List<Triple<String, String, String>>,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onBedrockVerChange: (String) -> Unit,
    onPaletteChange: (Int) -> Unit,
    onQuickPresetClick: (String, String, String, Int) -> Unit,
    onGenerateClick: () -> Unit
) {
    Surface(
        color = RoxyCardSurface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Generador de Add-ons con IA (.mcaddon)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Incluye Spawn Natural en Biomas + Tabla de Botín + Skin 64x64",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoxyTextSecondary
                        )
                    }
                }

                val (_, pHex, sHex) = palettes[selectedPaletteIndex]
                FullBodyMinecraftSkinPreview(
                    primaryHex = pHex,
                    secondaryHex = sHex,
                    modifier = Modifier.size(width = 48.dp, height = 64.dp)
                )
            }

            // Quick Idea Presets
            Text(
                text = "Plantillas rápidas (toca para cargar):",
                style = MaterialTheme.typography.labelSmall,
                color = RoxyTextMuted
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickPresetClick(
                            "Felix • Compañero NPC Leal",
                            "NPC_COMPANION",
                            "Un compañero NPC estilo femboy anime muy leal y afectuoso que me siga a todas partes, acepte flores o diamantes, converse conmigo con frases dulces y me defienda de cualquier enemigo.",
                            0
                        )
                    },
                    label = { Text("✨ Compañero NPC Leal / Anime") },
                    modifier = Modifier.testTag("preset_npc_companion")
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickPresetClick(
                            "Aria • Compañera Cariñosa & Guardiana",
                            "NPC_COMPANION",
                            "Una compañera cariñosa que camine a mi lado en Minecraft Bedrock, tenga diálogos personalizados, acepte regalos como manzanas doradas o flores y cure/proteja a mi personaje.",
                            1
                        )
                    },
                    label = { Text("💖 Compañera / Guardiana") },
                    modifier = Modifier.testTag("preset_companion_guardian")
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        onQuickPresetClick(
                            "Guadaña de Plasma Roxy",
                            "WEAPON_ITEM",
                            "Arma mítica con receta de crafteo, alto daño cuerpo a cuerpo, brillo encantado e invocación de aliado.",
                            2
                        )
                    },
                    label = { Text("⚔️ Arma + Invocación") }
                )
            }

            OutlinedTextField(
                value = customModName,
                onValueChange = onNameChange,
                label = { Text("Nombre del Personaje o Mod") },
                placeholder = { Text("Ej. Felix • Compañero Leal") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mod_name_input")
            )

            OutlinedTextField(
                value = promptText,
                onValueChange = onPromptChange,
                label = { Text("Describe a la IA cómo quieres tu mod") },
                placeholder = {
                    Text("Describe su personalidad, aspecto, qué objetos acepta, cómo te sigue o defiende y sus frases…")
                },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mod_prompt_input")
            )

            // Version Row
            Text(
                text = "Versión de Minecraft Bedrock (Android / Todas las versiones):",
                style = MaterialTheme.typography.labelSmall,
                color = RoxyTextMuted
            )
            val versions = listOf("1.21.70+", "1.21.50", "1.21.0", "1.20.80")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                versions.forEach { ver ->
                    FilterChip(
                        selected = selectedBedrockVer == ver,
                        onClick = { onBedrockVerChange(ver) },
                        label = {
                            Text(
                                text = if (ver == "1.21.70+") "MC $ver (Última)" else "MC $ver",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Skin Palette Selector
            Text(
                text = "Estilo de Skin 64x64 y Huevo Generador:",
                style = MaterialTheme.typography.labelSmall,
                color = RoxyTextMuted
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                palettes.forEachIndexed { idx, (label, _, _) ->
                    FilterChip(
                        selected = selectedPaletteIndex == idx,
                        onClick = { onPaletteChange(idx) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            if (isGenerating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onGenerateClick,
                enabled = (promptText.isNotBlank() || customModName.isNotBlank()) && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_mod_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generar Mod y Compilar Pack (.mcaddon)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MinecraftModCard(
    mod: MinecraftModEntity,
    onEditBehaviors: () -> Unit,
    onInspectJson: () -> Unit,
    onValidatePackage: () -> Unit,
    onCycleSkin: () -> Unit,
    onDelete: () -> Unit,
    onSaveMcAddonFile: () -> Unit,
    onShareToMinecraft: () -> Unit
) {
    Surface(
        color = RoxyCardSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("minecraft_mod_card_${mod.id}")
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
                FullBodyMinecraftSkinPreview(
                    primaryHex = mod.primaryColorHex,
                    secondaryHex = mod.secondaryColorHex,
                    modifier = Modifier.size(width = 48.dp, height = 64.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mod.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${mod.entityIdentifier} • Bedrock ${mod.bedrockVersion}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Spawn Natural en Overworld + Huevo + /summon",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoxyEmerald
                    )
                }
                IconButton(
                    onClick = onCycleSkin,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Variar paleta de skin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar mod",
                        tint = RoxyTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = mod.promptUsed,
                style = MaterialTheme.typography.bodyMedium,
                color = RoxyTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Behavior & Stats Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(label = "❤️ ${mod.maxHealth} HP", color = RoxyRose)
                StatBadge(label = "⚔️ ${mod.attackDamage} ATQ", color = RoxyAmber)
                StatBadge(
                    label = "🎁 Regalo: ${mod.tameItem.substringAfter(":")}",
                    color = RoxyPurple
                )
                if (mod.followsPlayer) {
                    StatBadge(label = "👣 Te sigue siempre", color = RoxyEmerald)
                }
                if (mod.defendsPlayer) {
                    StatBadge(label = "🛡️ Te protege", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val firstLine = mod.dialogueLines.lines().firstOrNull { it.isNotBlank() }
                ?: "¡Hola! Estoy listo para acompañarte."
            Surface(
                color = RoxyCardElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💬 \"$firstLine\"",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary actions: Customize behaviors, View JSON, Validate 11-File Pack
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onEditBehaviors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_mod_${mod.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar IA", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onInspectJson,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inspect_json_${mod.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JSON BP/RP", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onValidatePackage,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("validate_pack_${mod.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = RoxyEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("12 Archivos", style = MaterialTheme.typography.labelMedium, color = RoxyEmerald)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Export CTA Row (.mcaddon Save & Open in Minecraft)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveMcAddonFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_mcaddon_${mod.id}")
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
                    onClick = onShareToMinecraft,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoxyEmerald,
                        contentColor = Color(0xFF002619)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_in_minecraft_${mod.id}")
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
        }
    }
}

@Composable
private fun StatBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Draws a live 8x8 Minecraft pixel-art character face preview matching the generated 64x64 skin.
 */
@Composable
fun PixelArtAvatarPreview(
    primaryHex: String,
    secondaryHex: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(primaryHex) {
        try {
            Color(android.graphics.Color.parseColor(primaryHex))
        } catch (_: Exception) {
            Color(0xFFF472B6)
        }
    }
    val secondaryColor = remember(secondaryHex) {
        try {
            Color(android.graphics.Color.parseColor(secondaryHex))
        } catch (_: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(12.dp))
    ) {
        val w = size.width
        val h = size.height
        val cellW = w / 8f
        val cellH = h / 8f

        drawRect(color = secondaryColor.copy(alpha = 0.35f), size = size)

        // Face skin base
        drawRect(
            color = Color(0xFFFFDFC4),
            topLeft = Offset(cellW * 1f, cellH * 2f),
            size = Size(cellW * 6f, cellH * 5f)
        )

        // Hair top & side locks
        drawRect(
            color = primaryColor,
            topLeft = Offset(cellW * 1f, cellH * 0.5f),
            size = Size(cellW * 6f, cellH * 2.5f)
        )
        drawRect(
            color = primaryColor,
            topLeft = Offset(cellW * 0.5f, cellH * 2f),
            size = Size(cellW * 1.2f, cellH * 4.5f)
        )
        drawRect(
            color = primaryColor,
            topLeft = Offset(cellW * 6.3f, cellH * 2f),
            size = Size(cellW * 1.2f, cellH * 4.5f)
        )

        // Eyes
        drawRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(cellW * 2f, cellH * 4f),
            size = Size(cellW * 1.3f, cellH * 1.2f)
        )
        drawRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(cellW * 4.7f, cellH * 4f),
            size = Size(cellW * 1.3f, cellH * 1.2f)
        )

        // Blush cheeks
        drawRect(
            color = Color(0xFFF43F5E).copy(alpha = 0.55f),
            topLeft = Offset(cellW * 1.6f, cellH * 5.3f),
            size = Size(cellW * 1.1f, cellH * 0.6f)
        )
        drawRect(
            color = Color(0xFFF43F5E).copy(alpha = 0.55f),
            topLeft = Offset(cellW * 5.3f, cellH * 5.3f),
            size = Size(cellW * 1.1f, cellH * 0.6f)
        )

        // Collar / Outfit bottom row
        drawRect(
            color = secondaryColor,
            topLeft = Offset(cellW * 1.5f, cellH * 7f),
            size = Size(cellW * 5f, cellH * 1f)
        )
    }
}

/**
 * Full-body 2D Minecraft character preview (Head, Hair, Torso, Arms, Striped Legs/Boots).
 */
@Composable
fun FullBodyMinecraftSkinPreview(
    primaryHex: String,
    secondaryHex: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(primaryHex) {
        try {
            Color(android.graphics.Color.parseColor(primaryHex))
        } catch (_: Exception) {
            Color(0xFFF472B6)
        }
    }
    val secondaryColor = remember(secondaryHex) {
        try {
            Color(android.graphics.Color.parseColor(secondaryHex))
        } catch (_: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(12.dp))
    ) {
        val w = size.width
        val h = size.height
        val uW = w / 16f
        val uH = h / 32f

        // Subtle aura background
        drawRect(color = Color(0xFF10131C), size = size)

        // Head (x: 4..12, y: 2..10)
        drawRect(
            color = Color(0xFFFFE2C8),
            topLeft = Offset(uW * 4f, uH * 2f),
            size = Size(uW * 8f, uH * 8f)
        )
        // Hair & side locks
        drawRect(
            color = primaryColor,
            topLeft = Offset(uW * 3.5f, uH * 1.5f),
            size = Size(uW * 9f, uH * 3.5f)
        )
        drawRect(
            color = primaryColor,
            topLeft = Offset(uW * 3.5f, uH * 5f),
            size = Size(uW * 1.5f, uH * 6f)
        )
        drawRect(
            color = primaryColor,
            topLeft = Offset(uW * 11f, uH * 5f),
            size = Size(uW * 1.5f, uH * 6f)
        )
        // Eyes
        drawRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(uW * 5.5f, uH * 6f),
            size = Size(uW * 1.5f, uH * 1.5f)
        )
        drawRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(uW * 9f, uH * 6f),
            size = Size(uW * 1.5f, uH * 1.5f)
        )

        // Torso Outfit (x: 4..12, y: 10..20)
        drawRect(
            color = secondaryColor,
            topLeft = Offset(uW * 4f, uH * 10f),
            size = Size(uW * 8f, uH * 10f)
        )
        // Collar / ribbon
        drawRect(
            color = primaryColor,
            topLeft = Offset(uW * 6f, uH * 10.5f),
            size = Size(uW * 4f, uH * 2.5f)
        )

        // Left & Right Arms (x: 1.5..4 and 12..14.5, y: 10..20)
        drawRect(
            color = secondaryColor,
            topLeft = Offset(uW * 1.5f, uH * 10f),
            size = Size(uW * 2.5f, uH * 8f)
        )
        drawRect(
            color = secondaryColor,
            topLeft = Offset(uW * 12f, uH * 10f),
            size = Size(uW * 2.5f, uH * 8f)
        )
        drawRect(
            color = Color(0xFFFFE2C8),
            topLeft = Offset(uW * 1.5f, uH * 18f),
            size = Size(uW * 2.5f, uH * 2f)
        )
        drawRect(
            color = Color(0xFFFFE2C8),
            topLeft = Offset(uW * 12f, uH * 18f),
            size = Size(uW * 2.5f, uH * 2f)
        )

        // Legs with thigh-high / boot stripes (x: 4..7.8 and 8.2..12, y: 20..30)
        drawRect(
            color = Color(0xFF191824),
            topLeft = Offset(uW * 4f, uH * 20f),
            size = Size(uW * 3.8f, uH * 10f)
        )
        drawRect(
            color = Color(0xFF191824),
            topLeft = Offset(uW * 8.2f, uH * 20f),
            size = Size(uW * 3.8f, uH * 10f)
        )
        drawRect(
            color = primaryColor,
            topLeft = Offset(uW * 4f, uH * 22f),
            size = Size(uW * 3.8f, uH * 1.6f)
        )
        drawRect(
            color = primaryColor,
            topLeft = Offset(uW * 8.2f, uH * 22f),
            size = Size(uW * 3.8f, uH * 1.6f)
        )
    }
}

@Composable
private fun AddonPackageValidatorDialog(
    mod: MinecraftModEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val report = remember(mod) { BedrockAddonCompiler.inspectAddonPackageManifest(mod) }
    var copiedFeedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoxyCardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = RoxyEmerald
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validador .mcaddon (${report.entries.size} Archivos)", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Paquete: ${report.fileName} (${report.totalSizeBytes / 1024} KB)\nCompatible con Minecraft Bedrock ${mod.bedrockVersion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RoxyTextSecondary
                )

                // Copyable In-Game Commands
                Surface(
                    color = RoxyCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Comandos dentro de Minecraft:",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoxyTextMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = report.summonCommand,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    copyToClipboardSafe(context, "Comando Summon", report.summonCommand)
                                    copiedFeedback = "¡Comando /summon copiado!"
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar comando summon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = report.giveTokenCommand,
                                style = MaterialTheme.typography.labelMedium,
                                color = RoxyEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    copyToClipboardSafe(context, "Comando Give", report.giveTokenCommand)
                                    copiedFeedback = "¡Comando /give copiado!"
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar comando give",
                                    tint = RoxyEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                if (copiedFeedback != null) {
                    Text(
                        text = copiedFeedback.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = RoxyEmerald
                    )
                }

                Text(
                    text = "Estructura interna verificada (BP + RP):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                report.entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (entry.packType == "BP") MaterialTheme.colorScheme.primary else RoxyPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = entry.path,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${entry.sizeBytes} B",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoxyTextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}

fun copyToClipboardSafe(context: Context, label: String, text: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}

@Composable
private fun ModBehaviorEditorDialog(
    mod: MinecraftModEntity,
    onDismiss: () -> Unit,
    onSave: (MinecraftModEntity) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(mod.name) }
    var maxHealth by rememberSaveable { mutableStateOf(mod.maxHealth.toString()) }
    var attackDamage by rememberSaveable { mutableStateOf(mod.attackDamage.toString()) }
    var tameItem by rememberSaveable { mutableStateOf(mod.tameItem) }
    var followsPlayer by rememberSaveable { mutableStateOf(mod.followsPlayer) }
    var defendsPlayer by rememberSaveable { mutableStateOf(mod.defendsPlayer) }
    var dialogueLines by rememberSaveable { mutableStateOf(mod.dialogueLines) }
    var bedrockVer by rememberSaveable { mutableStateOf(mod.bedrockVersion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoxyCardSurface,
        title = {
            Text("Personalizar Comportamiento y Diálogos", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre en Minecraft") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = maxHealth,
                        onValueChange = { maxHealth = it },
                        label = { Text("Vida (HP)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = attackDamage,
                        onValueChange = { attackDamage = it },
                        label = { Text("Ataque") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = tameItem,
                    onValueChange = { tameItem = it },
                    label = { Text("Objeto para domesticar / regalar") },
                    placeholder = { Text("minecraft:poppy, minecraft:diamond…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bedrockVer,
                    onValueChange = { bedrockVer = it },
                    label = { Text("Versión Minecraft Bedrock") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Seguir al jugador automáticamente", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = followsPlayer, onCheckedChange = { followsPlayer = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Defender al jugador de monstruos", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = defendsPlayer, onCheckedChange = { defendsPlayer = it })
                }
                OutlinedTextField(
                    value = dialogueLines,
                    onValueChange = { dialogueLines = it },
                    label = { Text("Frases y diálogos (1 por línea)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        mod.copy(
                            name = name.ifBlank { mod.name },
                            maxHealth = maxHealth.toIntOrNull()?.coerceIn(10, 2000) ?: mod.maxHealth,
                            attackDamage = attackDamage.toIntOrNull()?.coerceIn(1, 500) ?: mod.attackDamage,
                            tameItem = tameItem.ifBlank { "minecraft:poppy" },
                            bedrockVersion = bedrockVer.ifBlank { "1.21.70+" },
                            followsPlayer = followsPlayer,
                            defendsPlayer = defendsPlayer,
                            dialogueLines = dialogueLines
                        )
                    )
                }
            ) {
                Text("Guardar y Recompilar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ModJsonInspectorDialog(
    mod: MinecraftModEntity,
    onDismiss: () -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(0) }
    val bpEntityJson = remember(mod) { BedrockAddonCompiler.generateBehaviorEntityJson(mod) }
    val bpSpawnRulesJson = remember(mod) { BedrockAddonCompiler.generateSpawnRulesJson(mod) }
    val rpEntityJson = remember(mod) { BedrockAddonCompiler.generateResourceClientEntityJson(mod) }
    val langContent = remember(mod) { BedrockAddonCompiler.generateLangContent(mod) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoxyCardSurface,
        title = {
            Text("Inspector de Código Bedrock (.mcaddon)", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("BP entity.json") }
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("BP spawn_rules.json") }
                    )
                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("RP client.json") }
                    )
                    FilterChip(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        label = { Text("es_ES.lang") }
                    )
                }

                Surface(
                    color = RoxyCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val codeToShow = when (activeTab) {
                        1 -> bpSpawnRulesJson
                        2 -> rpEntityJson
                        3 -> langContent
                        else -> bpEntityJson
                    }
                    Text(
                        text = codeToShow,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar Inspector")
            }
        }
    )
}
