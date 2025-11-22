package com.horizone.pep_notes.ui.people

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.*
import androidx.navigation.*

import com.horizone.pep_notes.data.model.*
import com.horizone.pep_notes.ui.nav.*
import com.horizone.pep_notes.ui.dialogs.*
import com.horizone.pep_notes.util.*
import com.horizone.pep_notes.viewmodel.*


@Composable
fun PeopleListScreen(
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel(),
    labelViewModel: LabelViewModel = hiltViewModel()
) {
    val allPersons by viewModel.allPersons.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allPersonLabels by labelViewModel.allPersonLabels.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }
    var personLabelsMap by remember { mutableStateOf<Map<Int, List<PersonLabel>>>(emptyMap()) }

    val displayedPersons = if (searchQuery.isEmpty()) allPersons else searchResults
    
    // Load labels for all displayed persons
    displayedPersons.forEach { person ->
        val labelsFlow = viewModel.getPersonLabels(person.id).collectAsState(initial = emptyList())
        personLabelsMap = personLabelsMap.toMutableMap().apply {
            this[person.id] = labelsFlow.value
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search bar and Label button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar (80% width)
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .weight(0.8f)
                        .height(56.dp),
                    placeholder = { Text("Search people...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
                
                // Label button (20% width)
                androidx.compose.material3.Button(
                    onClick = { showLabelDialog = true },
                    modifier = Modifier
                        .weight(0.2f)
                        .height(56.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        text = "🏷️",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // People list
            if (displayedPersons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No people yet. Add one!" else "No results found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedPersons) { person ->
                        PersonCard(
                            person = person,
                            personLabels = personLabelsMap[person.id] ?: emptyList(),
                            onClick = {
                                navController.navigate(NavRoutes.PersonDetail.createRoute(person.id))
                            }
                        )
                    }
                }
            }
        }
    }

    // Add person dialog
    if (showAddDialog) {
        AddPersonDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, labelIds ->
                viewModel.createPersonWithLabels(name, labelIds)
                showAddDialog = false
            },
            allPersonLabels = allPersonLabels
        )
    }

    // Label management dialog
    if (showLabelDialog) {
        LabelManagementDialog(
            allPersonLabels = allPersonLabels,
            labelViewModel = labelViewModel,
            onDismiss = { showLabelDialog = false }
        )
    }
}

@Composable
fun PersonCard(
    person: Person,
    personLabels: List<com.horizone.pep_notes.data.model.PersonLabel> = emptyList(),
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
            // Person info
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium
                )
                // Spacer(modifier = Modifier.height(4.dp))
                // Text(
                //     text = "Created: ${DateFormatter.formatDate(person.createdAt)}",
                //     style = MaterialTheme.typography.bodySmall,
                //     color = MaterialTheme.colorScheme.onSurfaceVariant
                // )
            }
            
            if (personLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    personLabels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(android.graphics.Color.parseColor(label.colorCode)),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label.labelName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Set<Int>) -> Unit,
    allPersonLabels: List<PersonLabel> = emptyList()
) {
    var personName by remember { mutableStateOf("") }
    var selectedLabelIds by remember { mutableStateOf(setOf<Int>()) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Person") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Person name field
                TextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Label selection
                Text(
                    text = "Assign Labels (Max 2)",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allPersonLabels) { label ->
                        val isSelected = label.id in selectedLabelIds
                        Card(
                            modifier = Modifier
                                .clickable {
                                    selectedLabelIds = if (isSelected) {
                                        selectedLabelIds - label.id
                                    } else if (selectedLabelIds.size < 2) {
                                        selectedLabelIds + label.id
                                    } else {
                                        selectedLabelIds
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    Color(android.graphics.Color.parseColor(label.colorCode))
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Text(
                                text = label.labelName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (personName.isNotBlank()) {
                        onAdd(personName, selectedLabelIds)
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

@Composable
fun LabelManagementDialog(
    allPersonLabels: List<PersonLabel>,
    labelViewModel: LabelViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddPersonLabelDialog by remember { mutableStateOf(false) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manage Labels",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab buttons - symmetric and beautiful
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "👤 Person",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    androidx.compose.material3.Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "📝 Note",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                
                // Divider
                androidx.compose.material3.Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Content based on selected tab
                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { showAddPersonLabelDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add new label",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (allPersonLabels.isEmpty()) {
                            Text(
                                text = "No labels yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allPersonLabels) { label ->
                                    var showDeleteConfirm by remember { mutableStateOf(false) }
                                    var showEditConfirm by remember { mutableStateOf(false) }
                                    
                                    if (showDeleteConfirm) {
                                        androidx.compose.material3.AlertDialog(
                                            onDismissRequest = { showDeleteConfirm = false },
                                            title = { Text("Delete Label") },
                                            text = { Text("Are you sure you want to delete \"${label.labelName}\"?") },
                                            confirmButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = {
                                                        labelViewModel.deletePersonLabel(label)
                                                        showDeleteConfirm = false
                                                    }
                                                ) {
                                                    Text("Delete")
                                                }
                                            },
                                            dismissButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = { showDeleteConfirm = false }
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                    
                                    if (showEditConfirm) {
                                        var editedName by remember { mutableStateOf(label.labelName) }
                                        var editedColor by remember { mutableStateOf(label.colorCode) }
                                        var editErrorMessage by remember { mutableStateOf("") }
                                        
                                        val allColors = listOf(
                                            "#8B5CF6", // Purple
                                            "#EC4899", // Pink
                                            "#06B6D4", // Cyan
                                            "#F59E0B", // Amber
                                            "#10B981"  // Emerald
                                        )
                                        
                                        val usedColors = (allPersonLabels.filter { it.id != label.id }.map { it.colorCode }.toSet() + 
                                                         com.horizone.pep_notes.data.seed.DefaultPersonLabels.getDefaultColorCodes())
                                        val availableColors = allColors.filter { it !in usedColors }
                                        
                                        androidx.compose.material3.AlertDialog(
                                            onDismissRequest = { showEditConfirm = false },
                                            title = { Text("Edit Label") },
                                            text = {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = "Label Name",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                    )
                                                    TextField(
                                                        value = editedName,
                                                        onValueChange = {
                                                            editedName = it
                                                            editErrorMessage = ""
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 16.dp),
                                                        placeholder = { Text("Enter label name") },
                                                        colors = TextFieldDefaults.colors(
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                                        ),
                                                        singleLine = true
                                                    )
                                                    
                                                    Text(
                                                        text = "Select Color",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(bottom = 12.dp)
                                                    )
                                                    
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 16.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        availableColors.forEach { color ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .background(
                                                                        color = Color(android.graphics.Color.parseColor(color)),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                                    .clickable { editedColor = color }
                                                                    .then(
                                                                        if (editedColor == color) {
                                                                            Modifier
                                                                                .background(
                                                                                    color = Color.Transparent,
                                                                                    shape = RoundedCornerShape(8.dp)
                                                                                )
                                                                                .padding(2.dp)
                                                                                .background(
                                                                                    color = Color.White,
                                                                                    shape = RoundedCornerShape(6.dp)
                                                                                )
                                                                        } else {
                                                                            Modifier
                                                                        }
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                if (editedColor == color) {
                                                                    Text(
                                                                        text = "✓",
                                                                        color = Color.Black,
                                                                        style = MaterialTheme.typography.headlineSmall
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    
                                                    if (editErrorMessage.isNotEmpty()) {
                                                        Text(
                                                            text = editErrorMessage,
                                                            color = MaterialTheme.colorScheme.error,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            modifier = Modifier.padding(bottom = 12.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            confirmButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = {
                                                        when {
                                                            editedName.isBlank() -> {
                                                                editErrorMessage = "Label name cannot be empty"
                                                            }
                                                            com.horizone.pep_notes.data.seed.DefaultPersonLabels.isReservedLabelName(editedName) && editedName != label.labelName -> {
                                                                editErrorMessage = "This label name is reserved"
                                                            }
                                                            allPersonLabels.any { it.labelName.equals(editedName, ignoreCase = true) && it.id != label.id } -> {
                                                                editErrorMessage = "Label name already exists"
                                                            }
                                                            com.horizone.pep_notes.data.seed.DefaultPersonLabels.isReservedColorCode(editedColor) && editedColor != label.colorCode -> {
                                                                editErrorMessage = "This color is reserved"
                                                            }
                                                            allPersonLabels.any { it.colorCode == editedColor && it.id != label.id } -> {
                                                                editErrorMessage = "Color already used"
                                                            }
                                                            else -> {
                                                                val updatedLabel = label.copy(labelName = editedName, colorCode = editedColor)
                                                                labelViewModel.updatePersonLabel(updatedLabel)
                                                                showEditConfirm = false
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Text("Save")
                                                }
                                            },
                                            dismissButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = { showEditConfirm = false }
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        color = Color(android.graphics.Color.parseColor(label.colorCode)),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                            )
                                            Text(
                                                text = label.labelName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            androidx.compose.material3.IconButton(
                                                onClick = { showEditConfirm = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Edit label",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            
                                            androidx.compose.material3.IconButton(
                                                onClick = { showDeleteConfirm = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete label",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val allNoteLabels by labelViewModel.noteLabels.collectAsState(initial = emptyList())
                    var showAddNoteLabelDialog by remember { mutableStateOf(false) }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { showAddNoteLabelDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add new label",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (allNoteLabels.isEmpty()) {
                            Text(
                                text = "No labels yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allNoteLabels) { label ->
                                    var showDeleteConfirm by remember { mutableStateOf(false) }
                                    
                                    if (showDeleteConfirm) {
                                        androidx.compose.material3.AlertDialog(
                                            onDismissRequest = { showDeleteConfirm = false },
                                            title = { Text("Delete Label") },
                                            text = { Text("Are you sure you want to delete \"${label.labelName}\"?") },
                                            confirmButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = {
                                                        labelViewModel.deleteNoteLabel(label)
                                                        showDeleteConfirm = false
                                                    }
                                                ) {
                                                    Text("Delete")
                                                }
                                            },
                                            dismissButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = { showDeleteConfirm = false }
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        color = Color(android.graphics.Color.parseColor(label.colorCode)),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                            )
                                            Text(
                                                text = label.labelName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            androidx.compose.material3.IconButton(
                                                onClick = { showDeleteConfirm = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete label",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (showAddNoteLabelDialog) {
                        com.horizone.pep_notes.ui.dialogs.AddNoteLabelDialog(
                            existingLabels = allNoteLabels,
                            onDismiss = { showAddNoteLabelDialog = false },
                            onConfirm = { newLabel ->
                                labelViewModel.createNoteLabel(newLabel.labelName, newLabel.colorCode)
                                showAddNoteLabelDialog = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    )

    // Add Person Label Dialog
    if (showAddPersonLabelDialog) {
        AddPersonLabelDialog(
            existingLabels = allPersonLabels,
            onDismiss = { showAddPersonLabelDialog = false },
            onConfirm = { newLabel ->
                labelViewModel.createPersonLabel(newLabel.labelName, newLabel.colorCode)
                showAddPersonLabelDialog = false
            }
        )
    }
}
