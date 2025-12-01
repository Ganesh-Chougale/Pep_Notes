package com.horizone.pep_notes.ui.nav

sealed class NavRoutes(val route: String) {
    data object PeopleList : NavRoutes("people_list")
    
    data object PersonDetail : NavRoutes("person_detail/{personId}") {
        fun createRoute(personId: Int) = "person_detail/$personId"
    }
    
    data object PersonEdit : NavRoutes("person_edit/{personId}") {
        fun createRoute(personId: Int? = null) =
            if (personId == null) "person_edit/-1" else "person_edit/$personId"
    }

    data object PersonNotes : NavRoutes("person_notes/{personId}") {
        fun createRoute(personId: Int) = "person_notes/$personId"
    }

    data object NoteEdit : NavRoutes("note_edit/{noteId}") {
        fun createRoute(noteId: Int? = null) =
            if (noteId == null) "note_edit/-1" else "note_edit/$noteId"
    }

    data object PersonLabels : NavRoutes("person_labels")
    data object NoteLabels : NavRoutes("note_labels")
    data object ExportImport : NavRoutes("export_import")
    data object ThemePicker : NavRoutes("theme_picker")
    data object About : NavRoutes("about_us")
}
