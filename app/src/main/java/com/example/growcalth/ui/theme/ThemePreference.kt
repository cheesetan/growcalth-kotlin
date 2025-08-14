package com.example.growcalth.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val value: Int) {
    LIGHT(0),
    AUTO(1),
    DARK(2);
    
    companion object {
        fun fromValue(value: Int): ThemeMode = values().find { it.value == value } ?: AUTO
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceManager(private val context: Context) {
    
    private val THEME_MODE = intPreferencesKey("theme_mode")
    
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeValue = preferences[THEME_MODE] ?: ThemeMode.AUTO.value
            ThemeMode.fromValue(themeValue)
        }
    
    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.value
        }
    }
}