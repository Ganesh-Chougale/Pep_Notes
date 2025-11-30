package com.horizone.pep_notes.ui.nav

import androidx.compose.runtime.Composable
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    isForestDark: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.PeopleList.route
    ) {
        composable(NavRoutes.PeopleList.route) {
            PeopleListScreen(
                navController = navController,
                onToggleTheme = onToggleTheme
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
                navController = navController,
                onToggleTheme = onToggleTheme
            )
        }

        composable(NavRoutes.NoteEdit.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: -1
            NoteEditScreen(
                noteId = noteId,
                navController = navController,
                onToggleTheme = onToggleTheme
            )
        }

        composable(NavRoutes.PersonLabels.route) {
            PersonLabelsScreen(
                navController = navController,
                onToggleTheme = onToggleTheme
            )
        }

        composable(NavRoutes.NoteLabels.route) {
            NoteLabelsScreen(
                navController = navController,
                onToggleTheme = onToggleTheme
            )
        }

        composable(NavRoutes.ExportImport.route) {
            ExportImportScreen(
                navController = navController,
                onToggleTheme = onToggleTheme
            )
        }
    }
}
