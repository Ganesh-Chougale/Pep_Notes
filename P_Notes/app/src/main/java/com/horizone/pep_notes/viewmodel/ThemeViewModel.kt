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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

private const val THEME_PREFERENCES_NAME = "theme_preferences"

private val Context.themeDataStore by preferencesDataStore(
    name = THEME_PREFERENCES_NAME
)

private object ThemePreferencesKeys {
    val IS_FOREST_DARK = booleanPreferencesKey("is_forest_dark")
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isForestDark = MutableStateFlow(false)
    val isForestDark: StateFlow<Boolean> = _isForestDark.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = context.themeDataStore.data.first()
            val saved = prefs[ThemePreferencesKeys.IS_FOREST_DARK] ?: false
            _isForestDark.value = saved
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newValue = !_isForestDark.value
            _isForestDark.value = newValue
            context.themeDataStore.edit { prefs ->
                prefs[ThemePreferencesKeys.IS_FOREST_DARK] = newValue
            }
        }
    }
}
