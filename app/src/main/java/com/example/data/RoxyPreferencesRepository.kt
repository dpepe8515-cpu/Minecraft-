package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "roxy_user_prefs")

data class UserPreferencesState(
    val developerHandle: String = "@dev_roxy",
    val apiBaseUrl: String = "https://roxy.gg/v1",
    val apiKey: String = "",
    val defaultModel: String = "claude-3.7-sonnet",
    val accentThemeIndex: Int = 0, // 0 = Cyber Cyan, 1 = Matrix Emerald, 2 = Neon Violet
    val skeletonDurationMs: Long = 850L,
    val reduceMotion: Boolean = false,
    val autoIsolateWorktrees: Boolean = true
)

class RoxyPreferencesRepository(private val context: Context) {

    private object Keys {
        val DEVELOPER_HANDLE = stringPreferencesKey("developer_handle")
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val API_KEY = stringPreferencesKey("api_key")
        val DEFAULT_MODEL = stringPreferencesKey("default_model")
        val ACCENT_THEME_INDEX = intPreferencesKey("accent_theme_index")
        val SKELETON_DURATION_MS = longPreferencesKey("skeleton_duration_ms")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val AUTO_ISOLATE_WORKTREES = booleanPreferencesKey("auto_isolate_worktrees")
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.dataStore.data.map { prefs ->
        UserPreferencesState(
            developerHandle = prefs[Keys.DEVELOPER_HANDLE] ?: "@dev_roxy",
            apiBaseUrl = prefs[Keys.API_BASE_URL] ?: "https://roxy.gg/v1",
            apiKey = prefs[Keys.API_KEY] ?: "",
            defaultModel = prefs[Keys.DEFAULT_MODEL] ?: "claude-3.7-sonnet",
            accentThemeIndex = prefs[Keys.ACCENT_THEME_INDEX] ?: 0,
            skeletonDurationMs = prefs[Keys.SKELETON_DURATION_MS] ?: 850L,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            autoIsolateWorktrees = prefs[Keys.AUTO_ISOLATE_WORKTREES] ?: true
        )
    }

    suspend fun updateDeveloperHandle(handle: String) {
        context.dataStore.edit { it[Keys.DEVELOPER_HANDLE] = handle.ifBlank { "@dev_roxy" } }
    }

    suspend fun updateApiConfig(baseUrl: String, apiKey: String, defaultModel: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_BASE_URL] = baseUrl.ifBlank { "https://roxy.gg/v1" }
            prefs[Keys.API_KEY] = apiKey.trim()
            prefs[Keys.DEFAULT_MODEL] = defaultModel.ifBlank { "claude-3.7-sonnet" }
        }
    }

    suspend fun updateAccentTheme(index: Int) {
        context.dataStore.edit { it[Keys.ACCENT_THEME_INDEX] = index.coerceIn(0, 2) }
    }

    suspend fun updateSkeletonSettings(durationMs: Long, reduceMotion: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SKELETON_DURATION_MS] = durationMs.coerceIn(300L, 2500L)
            prefs[Keys.REDUCE_MOTION] = reduceMotion
        }
    }

    suspend fun updateAutoIsolateWorktrees(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_ISOLATE_WORKTREES] = enabled }
    }
}
