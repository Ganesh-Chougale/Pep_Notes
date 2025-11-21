package com.horizone.pep_notes.ui.people

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.util.DateFormatter
import com.horizone.pep_notes.viewmodel.LabelViewModel
import com.horizone.pep_notes.viewmodel.PersonViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonEditScreen(
    personId: Int,
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel(),
    labelViewModel: LabelViewModel = hiltViewModel()
) {
    val selectedPerson by viewModel.selectedPerson.collectAsState()
    val allPersonLabels by labelViewModel.allPersonLabels.collectAsState(initial = emptyList())
    val personLabels by viewModel.personLabels.collectAsState(initial = emptyList())
    var personName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedLabelIds by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(personId) {
        if (personId != -1) {
            viewModel.selectPerson(Person(id = personId, name = ""))
        }
    }

    LaunchedEffect(selectedPerson) {
        selectedPerson?.let { person ->
            personName = person.name
        }
    }

    LaunchedEffect(personId) {
        if (personId != -1) {
            viewModel.loadPersonLabels(personId)
        }
    }

    LaunchedEffect(personLabels) {
        selectedLabelIds = personLabels.map { it.id }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (personId == -1) "New Person" else "Edit Person") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (personId != -1) {
                        IconButton(onClick = {
                            selectedPerson?.let { person ->
                                viewModel.deletePerson(person)
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Person name field
            TextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata
            if (selectedPerson != null) {
                Text(
                    text = "Created: ${DateFormatter.formatDate(selectedPerson!!.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Label Selection
            Text(
                text = "Assign Labels",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPersonLabels) { label ->
                    val isSelected = label.id in selectedLabelIds
                    Card(
                        modifier = Modifier
                            .clickable {
                                selectedLabelIds = if (isSelected) {
                                    selectedLabelIds - label.id
                                } else {
                                    selectedLabelIds + label.id
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                Color(android.graphics.Color.parseColor(label.colorCode))
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        border = if (isSelected) {
                            androidx.compose.material3.CardDefaults.outlinedCardBorder()
                        } else {
                            null
                        }
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

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (personName.isNotBlank()) {
                            if (personId == -1) {
                                viewModel.createPersonWithLabels(personName, selectedLabelIds)
                            } else {
                                selectedPerson?.let { person ->
                                    viewModel.updatePersonWithLabels(person.copy(name = personName), selectedLabelIds)
                                }
                            }
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = personName.isNotBlank() && !isLoading
                ) {
                    Text(if (personId == -1) "Create" else "Update")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
