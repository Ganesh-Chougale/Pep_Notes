package com.horizone.pep_notes.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.horizone.pep_notes.ui.people.PersonDetailScreen
import com.horizone.pep_notes.ui.people.PeopleListScreen
import com.horizone.pep_notes.ui.people.PersonEditScreen
import com.horizone.pep_notes.ui.notes.PersonNotesScreen
import com.horizone.pep_notes.ui.notes.NoteEditScreen
import com.horizone.pep_notes.ui.labels.PersonLabelsScreen
import com.horizone.pep_notes.ui.labels.NoteLabelsScreen
import com.horizone.pep_notes.ui.export.ExportImportScreen
import com.horizone.pep_notes.ui.about.AboutUsScreen
import com.horizone.pep_notes.ui.theme.AppTheme
import com.horizone.pep_notes.ui.theme.ThemePickerScreen
import com.horizone.pep_notes.viewmodel.ThemeState

@Composable
fun AppNavHost(
    navController: NavHostController,
    themeState: ThemeState,
    onThemeSelected: (AppTheme) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.PeopleList.route
    ) {
        composable(NavRoutes.PeopleList.route) {
            PeopleListScreen(
                navController = navController
            )
        }

        composable(NavRoutes.PersonDetail.route) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId")?.toIntOrNull() ?: -1
            PersonDetailScreen(
                personId = personId,
                navController = navController
            )
        }

        composable(NavRoutes.PersonEdit.route) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId")?.toIntOrNull() ?: -1
            PersonEditScreen(
                personId = personId,
                navController = navController
            )
        }

        composable(NavRoutes.PersonNotes.route) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId")?.toIntOrNull() ?: -1
            PersonNotesScreen(
                personId = personId,
                navController = navController
            )
        }

        composable(NavRoutes.NoteEdit.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: -1
            NoteEditScreen(
                noteId = noteId,
                navController = navController
            )
        }

        composable(NavRoutes.PersonLabels.route) {
            PersonLabelsScreen(
                navController = navController
            )
        }

        composable(NavRoutes.NoteLabels.route) {
            NoteLabelsScreen(
                navController = navController
            )
        }

        composable(NavRoutes.ExportImport.route) {
            ExportImportScreen(
                navController = navController
            )
        }

        composable(NavRoutes.ThemePicker.route) {
            ThemePickerScreen(
                currentTheme = themeState.appTheme,
                onThemeSelected = { appTheme ->
                    onThemeSelected(appTheme)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.About.route) {
            AboutUsScreen(navController = navController)
        }
    }
}
