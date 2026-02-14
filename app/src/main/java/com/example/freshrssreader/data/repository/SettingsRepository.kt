package com.example.freshrssreader.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ViewMode { COMPACT, MEDIUM, FULL }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class BrowserMode { INTERNAL, EXTERNAL }

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_VIEW_MODE = stringPreferencesKey("view_mode")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_BROWSER_MODE = stringPreferencesKey("browser_mode")
    }

    val viewMode: Flow<ViewMode> = dataStore.data.map { prefs ->
        prefs[KEY_VIEW_MODE]?.let { ViewMode.valueOf(it) } ?: ViewMode.MEDIUM
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    val browserMode: Flow<BrowserMode> = dataStore.data.map { prefs ->
        prefs[KEY_BROWSER_MODE]?.let { BrowserMode.valueOf(it) } ?: BrowserMode.INTERNAL
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { it[KEY_VIEW_MODE] = mode.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setBrowserMode(mode: BrowserMode) {
        dataStore.edit { it[KEY_BROWSER_MODE] = mode.name }
    }
}
