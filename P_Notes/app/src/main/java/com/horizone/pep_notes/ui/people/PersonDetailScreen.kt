package com.horizone.pep_notes.ui.people

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*

import androidx.hilt.navigation.compose.*

import androidx.navigation.*

import com.horizone.pep_notes.data.model.*
import com.horizone.pep_notes.ui.nav.*
import com.horizone.pep_notes.ui.dialogs.*
import com.horizone.pep_notes.util.*
import com.horizone.pep_notes.viewmodel.*

import java.time.*
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Int,
    navController: NavHostController,
    personViewModel: PersonViewModel = hiltViewModel(),
    noteViewModel: NoteViewModel = hiltViewModel(),
    labelViewModel: LabelViewModel = hiltViewModel()
) {
    val selectedPerson by personViewModel.selectedPerson.collectAsState()
    val notesForPerson by noteViewModel.notesForPerson.collectAsState()
    val allPersonLabels by labelViewModel.allPersonLabels.collectAsState(initial = emptyList())
    val allNoteLabels by labelViewModel.noteLabels.collectAsState(initial = emptyList())
    val personLabels by personViewModel.personLabels.collectAsState(initial = emptyList())
    var editedName by remember { mutableStateOf("") }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteConfirmationInput by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf("") }
    var selectedNoteLabelId by remember { mutableStateOf<Int?>(null) }
    var noteSearchQuery by remember { mutableStateOf("") }
    var showFinalDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var editedNoteTitle by remember { mutableStateOf("") }
    var editedNoteText by remember { mutableStateOf("") }
    var editedNoteLabelId by remember { mutableStateOf<Int?>(null) }
    var showEditNoteConfirmDialog by remember { mutableStateOf(false) }
    var selectedMonthYear by remember { mutableStateOf<YearMonth?>(null) }
    var isMonthYearDropdownExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(YearMonth.now().year) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now().monthValue) }
    var showPersonDetailsDialog by remember { mutableStateOf(false) }
    var noteSortOrder by remember { mutableStateOf("descending") }
    var isNoteSortMenuExpanded by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showDeleteNoteConfirmDialog by remember { mutableStateOf(false) }
    var showManageNoteLabelsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (personId != -1) {
            personViewModel.loadPersonById(personId)
            noteViewModel.loadNotesForPerson(personId)
            personViewModel.loadPersonLabels(personId)
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
                    // 1. Person name with Edit/Delete buttons (80% - 10% - 10% ratio)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Person name (80% width)
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(0.8f)
                                .clickable { showPersonDetailsDialog = true }
                        )

                        // Edit button (10% width)
                        IconButton(
                            onClick = {
                                editedName = person.name
                                showEditConfirmDialog = true
                            },
                            modifier = Modifier
                                .weight(0.1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Delete button (10% width)
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .weight(0.1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Month/Year filter with custom picker
                    val personCreatedYearMonth = YearMonth.from(person.createdAt)
                    val currentYearMonth = YearMonth.now()
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMonthYearDropdownExpanded = !isMonthYearDropdownExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                
                                // Search bar
                                TextField(
                                    value = noteSearchQuery,
                                    onValueChange = { noteSearchQuery = it },
                                    label = { Text("Search notes...") },
                                    placeholder = { Text("Title, text, or label") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search"
                                        )
                                    },
                                    trailingIcon = {
                                        if (noteSearchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { noteSearchQuery = "" },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear search"
                                                )
                                            }
                                        }
                                    },
                                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
                                )
                                
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

                    // 4. Notes section header with sorter and add button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sort filter for notes (left side)
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isNoteSortMenuExpanded = !isNoteSortMenuExpanded }
                            ) {
                                Icon(
                                    imageVector = if (noteSortOrder == "descending") Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Sort order",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = if (noteSortOrder == "descending") "Newest" else "Oldest",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
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
                        
                        // Add note button (right side)
                        Button(
                            onClick = { showAddNoteDialog = true },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+ Add Note")
                        }
                    }

                    // Filter notes by selected month/year and search query
                    val filteredNotes = notesForPerson.filter { note ->
                        // Month/year filter
                        val monthYearMatch = if (selectedMonthYear != null) {
                            YearMonth.from(note.createdAt) == selectedMonthYear
                        } else {
                            true
                        }
                        
                        // Search filter (title, text, or label)
                        val searchMatch = if (noteSearchQuery.isNotBlank()) {
                            val query = noteSearchQuery.lowercase()
                            val titleMatch = note.title.lowercase().contains(query)
                            val textMatch = note.text.lowercase().contains(query)
                            val labelMatch = if (note.labelId != null) {
                                allNoteLabels.find { it.id == note.labelId }?.labelName?.lowercase()?.contains(query) ?: false
                            } else {
                                false
                            }
                            titleMatch || textMatch || labelMatch
                        } else {
                            true
                        }
                        
                        monthYearMatch && searchMatch
                    }

                    // Notes list with sort order
                    if (filteredNotes.isEmpty()) {
                        Text(
                            text = when {
                                noteSearchQuery.isNotBlank() -> "No notes match your search"
                                selectedMonthYear != null -> "No notes in this month"
                                else -> "No notes yet"
                            },
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
                                    allNoteLabels = allNoteLabels,
                                    onEdit = {
                                        editingNote = note
                                        editedNoteTitle = note.title
                                        editedNoteText = note.text
                                        editedNoteLabelId = note.labelId
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
        val currentLabelIds = remember(personLabels) { personLabels.map { it.id }.toSet() }
        var selectedLabelIds by remember(personLabels) { mutableStateOf(currentLabelIds) }
        
        EditConfirmDialog(
            originalName = selectedPerson!!.name,
            editedName = editedName,
            onEditedNameChange = { editedName = it },
            onConfirm = {
                // Update person with new name and labels
                val updatedPerson = if (editedName != selectedPerson!!.name) {
                    selectedPerson!!.copy(name = editedName)
                } else {
                    selectedPerson!!
                }
                personViewModel.updatePersonWithLabels(updatedPerson, selectedLabelIds)
                showEditConfirmDialog = false
            },
            onDismiss = { showEditConfirmDialog = false },
            allPersonLabels = allPersonLabels,
            currentPersonLabels = personLabels,
            onLabelsChange = { labelIds ->
                selectedLabelIds = labelIds.toSet()
            }
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
            originalLabelId = editingNote!!.labelId,
            editedLabelId = editedNoteLabelId,
            onEditedTitleChange = { editedNoteTitle = it },
            onEditedTextChange = { editedNoteText = it },
            onEditedLabelChange = { editedNoteLabelId = it },
            availableLabels = allNoteLabels,
            onConfirm = {
                noteViewModel.updateNote(editingNote!!.copy(title = editedNoteTitle, text = editedNoteText, labelId = editedNoteLabelId))
                showEditNoteConfirmDialog = false
                editingNote = null
                editedNoteTitle = ""
                editedNoteText = ""
                editedNoteLabelId = null
            },
            onDismiss = {
                showEditNoteConfirmDialog = false
                editingNote = null
                editedNoteTitle = ""
                editedNoteText = ""
                editedNoteLabelId = null
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
            selectedLabelId = selectedNoteLabelId,
            onLabelChange = { selectedNoteLabelId = it },
            availableLabels = allNoteLabels,
            onManageLabels = { showManageNoteLabelsDialog = true },
            onAdd = {
                if (newNoteText.isNotBlank()) {
                    noteViewModel.createNote(selectedPerson!!.id, newNoteTitle, newNoteText, selectedNoteLabelId)
                    newNoteTitle = ""
                    newNoteText = ""
                    selectedNoteLabelId = null
                    showAddNoteDialog = false
                }
            },
            onDismiss = {
                newNoteTitle = ""
                newNoteText = ""
                selectedNoteLabelId = null
                showAddNoteDialog = false
            }
        )
    }

    if (showManageNoteLabelsDialog) {
        LabelManagementDialog(
            allPersonLabels = allPersonLabels,
            labelViewModel = labelViewModel,
            onDismiss = { showManageNoteLabelsDialog = false },
            initialTab = 1
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
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

}

@Composable
fun NoteCard(note: Note, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}, allNoteLabels: List<com.horizone.pep_notes.data.model.NoteLabel> = emptyList()) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    val labelColor = if (note.labelId != null) {
                        allNoteLabels.find { it.id == note.labelId }?.colorCode?.let {
                            Color(android.graphics.Color.parseColor(it))
                        } ?: MaterialTheme.colorScheme.primary
                    } else {
                        // When there is no label, use onSurface so the title is always high-contrast
                        MaterialTheme.colorScheme.onSurface
                    }

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = labelColor,
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
                        tint = MaterialTheme.colorScheme.onSurface
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
    onDismiss: () -> Unit,
    allPersonLabels: List<PersonLabel> = emptyList(),
    currentPersonLabels: List<PersonLabel> = emptyList(),
    onLabelsChange: (List<Int>) -> Unit = {}
) {
    var selectedLabelIds by remember { mutableStateOf(currentPersonLabels.map { it.id }.toSet()) }
    val originalLabelIds = currentPersonLabels.map { it.id }.toSet()
    
    // Check if there are any changes (name or labels)
    val hasNameChanged = editedName.isNotBlank() && editedName != originalName
    val hasLabelsChanged = selectedLabelIds != originalLabelIds
    val isApplyEnabled = hasNameChanged || hasLabelsChanged
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Person") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Current name: $originalName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextField(
                    value = editedName,
                    onValueChange = onEditedNameChange,
                    label = { Text("New name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Labels section
                Text(
                    text = "Assign/Remove Labels (Max 2)",
                    style = MaterialTheme.typography.labelMedium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allPersonLabels.forEach { label ->
                        val isSelected = label.id in selectedLabelIds
                        val canSelect = isSelected || selectedLabelIds.size < 2
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canSelect) {
                                    selectedLabelIds = if (label.id in selectedLabelIds) {
                                        selectedLabelIds - label.id
                                    } else {
                                        selectedLabelIds + label.id
                                    }
                                    onLabelsChange(selectedLabelIds.toList())
                                }
                                .alpha(if (canSelect) 1f else 0.5f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = if (isSelected) {
                                            Color(android.graphics.Color.parseColor(label.colorCode))
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = Color(android.graphics.Color.parseColor(label.colorCode)),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )

                            Text(
                                text = label.labelName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = isApplyEnabled
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
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
    selectedLabelId: Int? = null,
    onLabelChange: (Int?) -> Unit = {},
    availableLabels: List<com.horizone.pep_notes.data.model.NoteLabel> = emptyList(),
    onManageLabels: () -> Unit = {},
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLabelDropdownExpanded by remember { mutableStateOf(false) }
    var showLabelHint by remember { mutableStateOf(false) }
    val selectedLabel = availableLabels.find { it.id == selectedLabelId }

    LaunchedEffect(Unit) {
        if (availableLabels.isNotEmpty()) {
            showLabelHint = true
            delay(1600)
            showLabelHint = false
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Note")
                if (availableLabels.isNotEmpty()) {
                    Box {
                        val hasSelectedLabel = selectedLabel != null
                        val golden = Color(0xFFFFD700)
                        val ringProgress by animateFloatAsState(
                            targetValue = if (hasSelectedLabel || (showLabelHint && selectedLabel == null)) 1f else 0f,
                            animationSpec = tween(durationMillis = 600),
                            label = "noteLabelRing"
                        )

                        Box(
                            modifier = (if (showLabelHint && selectedLabel == null) {
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            } else {
                                Modifier
                            })
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isLabelDropdownExpanded = !isLabelDropdownExpanded
                                    showLabelHint = false
                                }
                                .drawBehind {
                                    if (ringProgress > 0f) {
                                        val strokeWidth = size.minDimension * 0.16f
                                        val radius = size.minDimension / 2f - strokeWidth / 2f
                                        drawCircle(
                                            color = golden,
                                            radius = radius * ringProgress.coerceIn(0f, 1f),
                                            style = Stroke(width = strokeWidth),
                                            alpha = 0.9f
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Label,
                                contentDescription = if (selectedLabel != null) "Change label" else "Add label",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = isLabelDropdownExpanded,
                            onDismissRequest = { isLabelDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    onLabelChange(null)
                                    isLabelDropdownExpanded = false
                                }
                            )
                            
                            availableLabels.forEach { label ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(
                                                        color = Color(android.graphics.Color.parseColor(label.colorCode)),
                                                        shape = RoundedCornerShape(3.dp)
                                                    )
                                            )
                                            Text(label.labelName)
                                        }
                                    },
                                    onClick = {
                                        onLabelChange(label.id)
                                        isLabelDropdownExpanded = false
                                    }
                                )
                            }

                            androidx.compose.material3.Divider()

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Manage Labels")
                                    }
                                },
                                onClick = {
                                    isLabelDropdownExpanded = false
                                    onManageLabels()
                                }
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = noteTitle,
                    onValueChange = onNoteTitleChange,
                    label = { Text("Note Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
                )
                
                TextField(
                    value = noteText,
                    onValueChange = onNoteTextChange,
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
                )

                if (selectedLabel != null) {
                    Text(
                        text = "Selected label",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val backgroundColor = Color(android.graphics.Color.parseColor(selectedLabel.colorCode))
                    val isLightColor =
                        backgroundColor.red * 0.299f +
                                backgroundColor.green * 0.587f +
                                backgroundColor.blue * 0.114f > 0.5f

                    Box(
                        modifier = Modifier
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedLabel.labelName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLightColor) Color.Black else Color.White
                        )
                    }
                }
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
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun EditNoteConfirmDialog(
    originalTitle: String,
    editedTitle: String,
    originalText: String,
    editedText: String,
    originalLabelId: Int? = null,
    editedLabelId: Int? = null,
    onEditedTitleChange: (String) -> Unit,
    onEditedTextChange: (String) -> Unit,
    onEditedLabelChange: (Int?) -> Unit = {},
    availableLabels: List<com.horizone.pep_notes.data.model.NoteLabel> = emptyList(),
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
                    singleLine = true,
                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
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
                    minLines = 3,
                    colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
                )

                // Label selection (Max 1)
                Text(
                    text = "Assign/Remove Label (Max 1)",
                    style = MaterialTheme.typography.labelMedium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableLabels.forEach { label ->
                        val isSelected = label.id == editedLabelId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newId = if (isSelected) null else label.id
                                    onEditedLabelChange(newId)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = if (isSelected) {
                                            Color(android.graphics.Color.parseColor(label.colorCode))
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = Color(android.graphics.Color.parseColor(label.colorCode)),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )

                            Text(
                                text = label.labelName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
