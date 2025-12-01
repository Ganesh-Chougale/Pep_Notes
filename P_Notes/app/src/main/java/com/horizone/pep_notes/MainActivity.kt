package com.horizone.pep_notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.horizone.pep_notes.ui.nav.AppNavHost
import com.horizone.pep_notes.ui.theme.AppTheme
import com.horizone.pep_notes.ui.theme.Pep_NotesTheme
import com.horizone.pep_notes.viewmodel.ThemeState
import com.horizone.pep_notes.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeState by themeViewModel.themeState.collectAsState()

            val onToggleTheme: () -> Unit = {
                themeViewModel.toggleTheme()
            }
            
            val onThemeSelected: (AppTheme) -> Unit = { appTheme ->
                themeViewModel.setTheme(appTheme)
            }

            Crossfade(targetState = themeState) { state ->
                Pep_NotesTheme(appTheme = state.appTheme, darkTheme = state.isDark) {
                    Scaffold(modifier = Modifier.fillMaxSize()) {
                        AppContent(
                            themeState = state,
                            onToggleTheme = onToggleTheme,
                            onThemeSelected = onThemeSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppContent(
    themeState: ThemeState,
    onToggleTheme: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    val navController = rememberNavController()
    AppNavHost(
        navController = navController,
        themeState = themeState,
        onToggleTheme = onToggleTheme,
        onThemeSelected = onThemeSelected
    )
}