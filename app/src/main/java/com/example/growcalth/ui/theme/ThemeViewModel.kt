package com.example.growcalth.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val themePreferenceManager = ThemePreferenceManager(application)
    
    val themeMode: StateFlow<ThemeMode> = themePreferenceManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.AUTO
        )
    
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themePreferenceManager.setThemeMode(themeMode)
        }
    }
}