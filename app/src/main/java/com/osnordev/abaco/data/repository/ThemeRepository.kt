package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user's dark/light theme preference and notification preference.
 * Requirements: 13.3, 13.4
 */
@Singleton
class ThemeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val themeKey = booleanPreferencesKey("dark_theme")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")

    fun isDarkTheme(): Flow<Boolean?> =
        dataStore.data.map { prefs -> prefs[themeKey] }

    suspend fun setDarkTheme(dark: Boolean) {
        dataStore.edit { prefs -> prefs[themeKey] = dark }
    }

    fun isNotificationsEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[notificationsKey] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[notificationsKey] = enabled }
    }
}
