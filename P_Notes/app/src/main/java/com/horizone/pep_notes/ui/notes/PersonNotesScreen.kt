package com.horizone.pep_notes.ui.notes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var sortOrder by remember { mutableStateOf("descending") }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (personId != -1) {
            noteViewModel.loadNotesForPerson(personId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${selectedPerson?.name ?: "Person"} Notes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Sort filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isSortMenuExpanded = !isSortMenuExpanded }
                    ) {
                        Icon(
                            imageVector = if (sortOrder == "descending") Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Sort order",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = if (sortOrder == "descending") "Newest" else "Oldest",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest First (Descending)") },
                            onClick = {
                                sortOrder = "descending"
                                isSortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest First (Ascending)") },
                            onClick = {
                                sortOrder = "ascending"
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }
            
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
                val sortedNotes = if (sortOrder == "descending") {
                    notes.sortedByDescending { it.createdAt }
                } else {
                    notes.sortedBy { it.createdAt }
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedNotes) { note ->
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
                noteViewModel.createNote(personId, "", text)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
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
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit note",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onAdd: (text: String) -> Unit
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
                maxLines = 5,
                colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
