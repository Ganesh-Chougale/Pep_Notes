package com.horizone.pep_notes.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.ui.nav.NavRoutes
import com.horizone.pep_notes.util.DateFormatter
import com.horizone.pep_notes.viewmodel.NoteViewModel
import com.horizone.pep_notes.viewmodel.PersonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Int,
    navController: NavHostController,
    personViewModel: PersonViewModel = hiltViewModel(),
    noteViewModel: NoteViewModel = hiltViewModel()
) {
    val selectedPerson by personViewModel.selectedPerson.collectAsState()
    val notesForPerson by noteViewModel.notesForPerson.collectAsState()
    var editedName by remember { mutableStateOf("") }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteConfirmationInput by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    var showFinalDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (personId != -1) {
            personViewModel.loadPersonById(personId)
            noteViewModel.loadNotesForPerson(personId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedPerson?.name ?: "Person Details") },
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
        }
    ) { innerPadding ->
        selectedPerson?.let { person ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 1. Person name (visible at top)
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Dropdown with Edit/Delete buttons
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = !isDropdownExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Dropdown header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Actions",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isDropdownExpanded) "Collapse" else "Expand"
                                )
                            }

                            // Expanded content
                            if (isDropdownExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            editedName = person.name
                                            showEditConfirmDialog = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit")
                                    }

                                    OutlinedButton(
                                        onClick = { showDeleteConfirmDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Date filter
                    Text(
                        text = "Created: ${DateFormatter.formatDate(person.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Notes section with Add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // 5. Add note button
                        Button(
                            onClick = { showAddNoteDialog = true },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+ Add Note")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes list in descending order
                    if (notesForPerson.isEmpty()) {
                        Text(
                            text = "No notes yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            notesForPerson.sortedByDescending { it.createdAt }.forEach { note ->
                                NoteCard(note = note)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit confirmation dialog
    if (showEditConfirmDialog && selectedPerson != null) {
        EditConfirmDialog(
            originalName = selectedPerson!!.name,
            editedName = editedName,
            onEditedNameChange = { editedName = it },
            onConfirm = {
                personViewModel.updatePerson(selectedPerson!!.copy(name = editedName))
                showEditConfirmDialog = false
            },
            onDismiss = { showEditConfirmDialog = false }
        )
    }

    // Delete confirmation dialog (name entry)
    if (showDeleteConfirmDialog && selectedPerson != null) {
        DeleteConfirmDialog(
            personName = selectedPerson!!.name,
            confirmationInput = deleteConfirmationInput,
            onConfirmationInputChange = { deleteConfirmationInput = it },
            onConfirm = {
                showFinalDeleteConfirmDialog = true
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                deleteConfirmationInput = ""
            }
        )
    }

    // Final delete confirmation dialog
    if (showFinalDeleteConfirmDialog && selectedPerson != null) {
        FinalDeleteConfirmDialog(
            personName = selectedPerson!!.name,
            onConfirm = {
                personViewModel.deletePerson(selectedPerson!!)
                showFinalDeleteConfirmDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showFinalDeleteConfirmDialog = false
            }
        )
    }

    // Add note dialog
    if (showAddNoteDialog && selectedPerson != null) {
        AddNoteDialog(
            noteText = newNoteText,
            onNoteTextChange = { newNoteText = it },
            onAdd = {
                if (newNoteText.isNotBlank()) {
                    noteViewModel.createNote(selectedPerson!!.id, newNoteText)
                    newNoteText = ""
                    showAddNoteDialog = false
                }
            },
            onDismiss = {
                newNoteText = ""
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
fun NoteCard(note: Note) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = DateFormatter.formatDate(note.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditConfirmDialog(
    originalName: String,
    editedName: String,
    onEditedNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Person") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Previous name: $originalName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextField(
                    value = editedName,
                    onValueChange = onEditedNameChange,
                    label = { Text("New name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Current name: $editedName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = editedName.isNotBlank() && editedName != originalName
            ) {
                Text("Apply Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    personName: String,
    confirmationInput: String,
    onConfirmationInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Person") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete \"$personName\"?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Type the person's name to confirm (case-insensitive):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextField(
                    value = confirmationInput,
                    onValueChange = onConfirmationInputChange,
                    label = { Text("Enter name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmationInput.trim().equals(personName, ignoreCase = true)
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddNoteDialog(
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            TextField(
                value = noteText,
                onValueChange = onNoteTextChange,
                label = { Text("Note content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = onAdd,
                enabled = noteText.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FinalDeleteConfirmDialog(
    personName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you absolutely sure you want to permanently delete \"$personName\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "This action cannot be undone. All notes associated with this person will also be deleted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Yes, Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
