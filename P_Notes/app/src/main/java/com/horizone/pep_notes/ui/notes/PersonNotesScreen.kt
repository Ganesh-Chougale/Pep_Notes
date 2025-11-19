package com.horizone.pep_notes.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.ui.components.LabelChip
import com.horizone.pep_notes.ui.nav.NavRoutes
import com.horizone.pep_notes.util.DateFormatter
import com.horizone.pep_notes.viewmodel.NoteViewModel
import com.horizone.pep_notes.viewmodel.PersonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonNotesScreen(
    personId: Int,
    navController: NavHostController,
    noteViewModel: NoteViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel()
) {
    val notes by noteViewModel.notesForPerson.collectAsState()
    val selectedPerson by personViewModel.selectedPerson.collectAsState()
    var showAddNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (personId != -1) {
            noteViewModel.loadNotesForPerson(personId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedPerson?.name ?: "Notes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes yet. Add one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                navController.navigate(NavRoutes.NoteEdit.createRoute(note.id))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddNoteDialog && personId != -1) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onAdd = { text ->
                noteViewModel.createNote(personId, text)
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.text.take(100) + if (note.text.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = DateFormatter.formatDateTime(note.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onAdd(noteText)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
