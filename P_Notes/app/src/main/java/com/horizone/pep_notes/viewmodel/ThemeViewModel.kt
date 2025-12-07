package com.horizone.pep_notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.horizone.pep_notes.ui.theme.AppTheme

private const val THEME_PREFERENCES_NAME = "theme_preferences"

private val Context.themeDataStore by preferencesDataStore(
    name = THEME_PREFERENCES_NAME
)

private object ThemePreferencesKeys {
    val APP_THEME = stringPreferencesKey("app_theme")
}

data class ThemeState(
    val appTheme: AppTheme = AppTheme.GLASS
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = context.themeDataStore.data.first()
            val storedThemeName = prefs[ThemePreferencesKeys.APP_THEME]
            val appTheme = storedThemeName?.let { name ->
                AppTheme.entries.find { it.name == name } ?: AppTheme.GLASS
            } ?: AppTheme.GLASS

            val newState = ThemeState(appTheme = appTheme)
            _themeState.value = newState
        }
    }

    fun setTheme(appTheme: AppTheme) {
        viewModelScope.launch {
            val newState = ThemeState(appTheme = appTheme)
            _themeState.value = newState
            context.themeDataStore.edit { prefs ->
                prefs[ThemePreferencesKeys.APP_THEME] = newState.appTheme.name
            }
        }
    }
}
