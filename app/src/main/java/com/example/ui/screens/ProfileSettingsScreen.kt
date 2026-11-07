package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserPreferencesState
import com.example.ui.theme.RoxyBorderSubtle
import com.example.ui.theme.RoxyCardElevated
import com.example.ui.theme.RoxyCardSurface
import com.example.ui.theme.RoxyTextSecondary
import kotlin.math.roundToLong

@Composable
fun ProfileSettingsScreen(
    preferences: UserPreferencesState,
    onSaveProfileAndApi: (String, String, String, String) -> Unit,
    onSelectAccentTheme: (Int) -> Unit,
    onUpdateSkeletonSettings: (Long, Boolean) -> Unit,
    onUpdateAutoIsolate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var handle by rememberSaveable { mutableStateOf(preferences.developerHandle) }
    var apiBaseUrl by rememberSaveable { mutableStateOf(preferences.apiBaseUrl) }
    var apiKey by rememberSaveable { mutableStateOf(preferences.apiKey) }
    var defaultModel by rememberSaveable { mutableStateOf(preferences.defaultModel) }
    var skeletonDurationSlider by rememberSaveable {
        mutableFloatStateOf(preferences.skeletonDurationMs.toFloat())
    }

    LaunchedEffect(preferences) {
        handle = preferences.developerHandle
        apiBaseUrl = preferences.apiBaseUrl
        apiKey = preferences.apiKey
        defaultModel = preferences.defaultModel
        skeletonDurationSlider = preferences.skeletonDurationMs.toFloat()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("profile_settings_list"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Perfil, APIs y Rendimiento",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configura tu identidad, el endpoint OpenAI-compatible (Roxy.gg / BYOM) y las animaciones Skeleton.",
                style = MaterialTheme.typography.bodyMedium,
                color = RoxyTextSecondary
            )
        }

        // Section 1: Developer Profile & OpenAI/Roxy API Endpoint
        item {
            Surface(
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Perfil y Endpoint OpenAI / Roxy.gg",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it },
                        label = { Text("Identificador de Desarrollador") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_handle_input")
                    )

                    OutlinedTextField(
                        value = apiBaseUrl,
                        onValueChange = { apiBaseUrl = it },
                        label = { Text("Base URL OpenAI-Compatible (Roxy v1)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_base_url_input")
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key (Opcional — vacío usa Sandbox Local)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_api_key_input")
                    )

                    OutlinedTextField(
                        value = defaultModel,
                        onValueChange = { defaultModel = it },
                        label = { Text("Modelo por defecto") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_model_input")
                    )

                    Button(
                        onClick = {
                            onSaveProfileAndApi(handle, apiBaseUrl, apiKey, defaultModel)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_api_settings_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar Perfil y Endpoint",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 2: Skeleton Screen Animation & Performance Settings
        item {
            Surface(
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Animaciones Skeleton y Rendimiento",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "Duración de transición Skeleton en feeds: ${skeletonDurationSlider.roundToLong()} ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoxyTextSecondary
                    )

                    Slider(
                        value = skeletonDurationSlider,
                        onValueChange = { skeletonDurationSlider = it },
                        onValueChangeFinished = {
                            onUpdateSkeletonSettings(
                                skeletonDurationSlider.roundToLong(),
                                preferences.reduceMotion
                            )
                        },
                        valueRange = 350f..2000f,
                        modifier = Modifier.testTag("skeleton_duration_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Dispositivos Gama Baja (Reducir movimiento)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Reemplaza el barrido diagonal por un pulso suave de bajo consumo de GPU.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoxyTextSecondary
                            )
                        }
                        Switch(
                            checked = preferences.reduceMotion,
                            onCheckedChange = { enabled ->
                                onUpdateSkeletonSettings(
                                    skeletonDurationSlider.roundToLong(),
                                    enabled
                                )
                            },
                            modifier = Modifier.testTag("reduce_motion_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aislar ramas Git Worktree automáticamente",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Crea un árbol independiente por cada agente paralelo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoxyTextSecondary
                            )
                        }
                        Switch(
                            checked = preferences.autoIsolateWorktrees,
                            onCheckedChange = onUpdateAutoIsolate,
                            modifier = Modifier.testTag("auto_isolate_switch")
                        )
                    }
                }
            }
        }

        // Section 3: Theme Accent Palette
        item {
            Surface(
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identidad Visual (Tema Oscuro)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val themes = listOf(
                        0 to "Cyber Cyan",
                        1 to "Matrix Emerald",
                        2 to "Neon Violet"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themes.forEach { (idx, name) ->
                            FilterChip(
                                selected = preferences.accentThemeIndex == idx,
                                onClick = { onSelectAccentTheme(idx) },
                                label = { Text(name) },
                                modifier = Modifier.testTag("theme_accent_$idx")
                            )
                        }
                    }
                }
            }
        }

        // Section 4: APK Build & API Configuration Guide (Requirement 5)
        item {
            Surface(
                color = RoxyCardSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, RoxyBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.apk_guide_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        color = RoxyCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "1. Generar APK en Android Studio:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• Abre el proyecto en Android Studio → menú Build > Build Bundle(s) / APK(s) > Build APK(s).\n• O ejecuta en terminal: gradle :app:assembleDebug\n• El archivo instalable se genera en: app/build/outputs/apk/debug/app-debug.apk",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoxyTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2. Configurar APIs (OpenAI / Roxy.gg y GitHub):",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• La búsqueda de repositorios GitHub en la pestaña 'Explorar' funciona directamente con la API pública oficial (https://api.github.com).\n• Para conectar inferencia en vivo con https://roxy.gg/v1 o cualquier proveedor compatible con OpenAI, ingresa tu Base URL y API Key arriba o en el panel Secrets de AI Studio.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoxyTextSecondary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
