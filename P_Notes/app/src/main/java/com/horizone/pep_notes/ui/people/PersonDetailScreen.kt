package com.horizone.pep_notes.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.ui.nav.NavRoutes
import com.horizone.pep_notes.util.DateFormatter
import com.horizone.pep_notes.viewmodel.NoteViewModel
import com.horizone.pep_notes.viewmodel.PersonViewModel
import java.time.YearMonth

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
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf("") }
    var showFinalDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var editedNoteTitle by remember { mutableStateOf("") }
    var editedNoteText by remember { mutableStateOf("") }
    var showEditNoteConfirmDialog by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedMonthYear by remember { mutableStateOf<YearMonth?>(null) }
    var isMonthYearDropdownExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(YearMonth.now().year) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now().monthValue) }
    var showPersonDetailsDialog by remember { mutableStateOf(false) }
    var noteSortOrder by remember { mutableStateOf("descending") }
    var isNoteSortMenuExpanded by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showDeleteNoteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (personId != -1) {
            personViewModel.loadPersonById(personId)
            noteViewModel.loadNotesForPerson(personId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HOME") },
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
                    // 1. Person name (clickable to show details)
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { showPersonDetailsDialog = true }
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

                    // 3. Month/Year filter with custom picker
                    val personCreatedYearMonth = YearMonth.from(person.createdAt)
                    val currentYearMonth = YearMonth.now()
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMonthYearDropdownExpanded = !isMonthYearDropdownExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedMonthYear != null) {
                                        "Filter: ${selectedMonthYear!!.month.value}/${selectedMonthYear!!.year}"
                                    } else {
                                        "All Notes"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = if (isMonthYearDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isMonthYearDropdownExpanded) "Collapse" else "Expand"
                                )
                            }

                            if (isMonthYearDropdownExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Clear filter button
                                if (selectedMonthYear != null) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedMonthYear = null
                                            isMonthYearDropdownExpanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Clear Filter")
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Month and Year dropdowns
                                var isMonthDropdownExpanded by remember { mutableStateOf(false) }
                                var isYearDropdownExpanded by remember { mutableStateOf(false) }
                                
                                val monthNames = listOf(
                                    "January", "February", "March", "April", "May", "June",
                                    "July", "August", "September", "October", "November", "December"
                                )
                                
                                // Generate available years
                                val availableYears = (personCreatedYearMonth.year..currentYearMonth.year).toList()
                                
                                // Generate available months for selected year
                                val availableMonths = (1..12).filter { month ->
                                    val isValid = when {
                                        selectedYear == personCreatedYearMonth.year && month < personCreatedYearMonth.monthValue -> false
                                        selectedYear == currentYearMonth.year && month > currentYearMonth.monthValue -> false
                                        else -> true
                                    }
                                    isValid
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Month Dropdown
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedButton(
                                            onClick = { isMonthDropdownExpanded = !isMonthDropdownExpanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(monthNames[selectedMonth - 1])
                                        }
                                        
                                        DropdownMenu(
                                            expanded = isMonthDropdownExpanded,
                                            onDismissRequest = { isMonthDropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.45f)
                                        ) {
                                            availableMonths.forEach { month ->
                                                DropdownMenuItem(
                                                    text = { Text(monthNames[month - 1]) },
                                                    onClick = {
                                                        selectedMonth = month
                                                        isMonthDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Year Dropdown
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedButton(
                                            onClick = { isYearDropdownExpanded = !isYearDropdownExpanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(selectedYear.toString())
                                        }
                                        
                                        DropdownMenu(
                                            expanded = isYearDropdownExpanded,
                                            onDismissRequest = { isYearDropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.45f)
                                        ) {
                                            availableYears.forEach { year ->
                                                DropdownMenuItem(
                                                    text = { Text(year.toString()) },
                                                    onClick = {
                                                        selectedYear = year
                                                        isYearDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Apply filter button
                                val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)
                                val isValidSelection = selectedYearMonth >= personCreatedYearMonth && selectedYearMonth <= currentYearMonth
                                
                                OutlinedButton(
                                    onClick = {
                                        if (isValidSelection) {
                                            selectedMonthYear = selectedYearMonth
                                            isMonthYearDropdownExpanded = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = isValidSelection
                                ) {
                                    Text("Apply Filter")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Notes section with filter and add button - all in one row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${person.name} Notes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sort filter for notes
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isNoteSortMenuExpanded = !isNoteSortMenuExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (noteSortOrder == "descending") Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Sort order",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(
                                        text = if (noteSortOrder == "descending") "Newest" else "Oldest",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = isNoteSortMenuExpanded,
                                    onDismissRequest = { isNoteSortMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Newest First (Descending)") },
                                        onClick = {
                                            noteSortOrder = "descending"
                                            isNoteSortMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Oldest First (Ascending)") },
                                        onClick = {
                                            noteSortOrder = "ascending"
                                            isNoteSortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                            
                            // Add note button
                            Button(
                                onClick = { showAddNoteDialog = true },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("+ Add Note")
                            }
                        }
                    }

                    // Filter notes by selected month/year
                    val filteredNotes = if (selectedMonthYear != null) {
                        notesForPerson.filter { note ->
                            YearMonth.from(note.createdAt) == selectedMonthYear
                        }
                    } else {
                        notesForPerson
                    }

                    // Notes list with sort order
                    if (filteredNotes.isEmpty()) {
                        Text(
                            text = if (selectedMonthYear != null) "No notes in this month" else "No notes yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val sortedNotes = if (noteSortOrder == "descending") {
                                filteredNotes.sortedByDescending { it.createdAt }
                            } else {
                                filteredNotes.sortedBy { it.createdAt }
                            }
                            
                            sortedNotes.forEach { note ->
                                NoteCard(
                                    note = note,
                                    onEdit = {
                                        editingNote = note
                                        editedNoteTitle = note.title
                                        editedNoteText = note.text
                                        showEditNoteConfirmDialog = true
                                    },
                                    onDelete = {
                                        noteToDelete = note
                                        showDeleteNoteConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Person details dialog
    if (showPersonDetailsDialog && selectedPerson != null) {
        PersonDetailsDialog(
            person = selectedPerson!!,
            notesCount = notesForPerson.size,
            onDismiss = { showPersonDetailsDialog = false }
        )
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

    // Edit note confirmation dialog
    if (showEditNoteConfirmDialog && editingNote != null) {
        EditNoteConfirmDialog(
            originalTitle = editingNote!!.title,
            editedTitle = editedNoteTitle,
            originalText = editingNote!!.text,
            editedText = editedNoteText,
            onEditedTitleChange = { editedNoteTitle = it },
            onEditedTextChange = { editedNoteText = it },
            onConfirm = {
                noteViewModel.updateNote(editingNote!!.copy(title = editedNoteTitle, text = editedNoteText))
                showEditNoteConfirmDialog = false
                editingNote = null
                editedNoteTitle = ""
                editedNoteText = ""
            },
            onDismiss = {
                showEditNoteConfirmDialog = false
                editingNote = null
                editedNoteTitle = ""
                editedNoteText = ""
            }
        )
    }

    // Add note dialog
    if (showAddNoteDialog && selectedPerson != null) {
        AddNoteDialog(
            noteTitle = newNoteTitle,
            onNoteTitleChange = { newNoteTitle = it },
            noteText = newNoteText,
            onNoteTextChange = { newNoteText = it },
            onAdd = {
                if (newNoteText.isNotBlank()) {
                    noteViewModel.createNote(selectedPerson!!.id, newNoteTitle, newNoteText)
                    newNoteTitle = ""
                    newNoteText = ""
                    showAddNoteDialog = false
                }
            },
            onDismiss = {
                newNoteTitle = ""
                newNoteText = ""
                showAddNoteDialog = false
            }
        )
    }

    // Delete note confirmation dialog
    if (showDeleteNoteConfirmDialog && noteToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteNoteConfirmDialog = false
                noteToDelete = null
            },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteViewModel.deleteNote(noteToDelete!!)
                        showDeleteNoteConfirmDialog = false
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteNoteConfirmDialog = false
                        noteToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NoteCard(note: Note, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Title and Edit/Delete buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.title.isNotEmpty()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit note",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Note text
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date
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
    noteTitle: String,
    onNoteTitleChange: (String) -> Unit,
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = noteTitle,
                    onValueChange = onNoteTitleChange,
                    label = { Text("Note Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                TextField(
                    value = noteText,
                    onValueChange = onNoteTextChange,
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
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

@Composable
fun PersonDetailsDialog(
    person: Person,
    notesCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Person Details") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Person name
                Column {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Created at
                Column {
                    Text(
                        text = "Created At",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateFormatter.formatDate(person.createdAt),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Number of notes
                Column {
                    Text(
                        text = "Number of Notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = notesCount.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EditNoteConfirmDialog(
    originalTitle: String,
    editedTitle: String,
    originalText: String,
    editedText: String,
    onEditedTitleChange: (String) -> Unit,
    onEditedTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Original vs Current Title
                if (originalTitle.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Original Title",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = originalTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit Title
                TextField(
                    value = editedTitle,
                    onValueChange = onEditedTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Original vs Current Text
                Column {
                    Text(
                        text = "Original Note",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = originalText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }

                // Edit Text
                TextField(
                    value = editedText,
                    onValueChange = onEditedTextChange,
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = editedText.isNotBlank()
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
