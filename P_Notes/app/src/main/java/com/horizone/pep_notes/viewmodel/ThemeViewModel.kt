package com.horizone.pep_notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.horizone.pep_notes.ui.theme.AppTheme

private const val THEME_PREFERENCES_NAME = "theme_preferences"

private val Context.themeDataStore by preferencesDataStore(
    name = THEME_PREFERENCES_NAME
)

private object ThemePreferencesKeys {
    val IS_FOREST_DARK = booleanPreferencesKey("is_forest_dark")
    val APP_THEME = stringPreferencesKey("app_theme")
}

data class ThemeState(
    val appTheme: AppTheme = AppTheme.GOTHAM,
    val isDark: Boolean = true
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()
    
    // Deprecated: For backward compatibility only
    @Deprecated("Use themeState instead", replaceWith = ReplaceWith("themeState"))
    val isForestDark: StateFlow<Boolean> = _themeState.map { it.isDark }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _themeState.value.isDark
    )

    init {
        viewModelScope.launch {
            val prefs = context.themeDataStore.data.first()
            val isDark = prefs[ThemePreferencesKeys.IS_FOREST_DARK] ?: true
            val storedThemeName = prefs[ThemePreferencesKeys.APP_THEME]
            val appTheme = storedThemeName?.let { name ->
                // Safely get theme from stored name, fallback to GOTHAM if unknown
                AppTheme.entries.find { it.name == name } ?: AppTheme.GOTHAM
            } ?: AppTheme.GOTHAM

            val newState = ThemeState(appTheme = appTheme, isDark = isDark)
            _themeState.value = newState
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = _themeState.value
            val newIsDark = !current.isDark
            val newState = current.copy(isDark = newIsDark)
_themeState.value = newState
            context.themeDataStore.edit { prefs ->
                prefs[ThemePreferencesKeys.IS_FOREST_DARK] = newIsDark
                prefs[ThemePreferencesKeys.APP_THEME] = newState.appTheme.name
            }
        }
    }

    fun setTheme(appTheme: AppTheme) {
        viewModelScope.launch {
            val current = _themeState.value
            val newState = current.copy(appTheme = appTheme)
            _themeState.value = newState
            context.themeDataStore.edit { prefs ->
                prefs[ThemePreferencesKeys.APP_THEME] = newState.appTheme.name
                prefs[ThemePreferencesKeys.IS_FOREST_DARK] = newState.isDark
            }
        }
    }
}
