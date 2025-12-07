package com.horizone.pep_notes.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.util.DateFormatter
import com.horizone.pep_notes.viewmodel.NoteViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Int,
    navController: NavHostController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val selectedNote by viewModel.selectedNote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val allNoteLabels by viewModel.noteLabels.collectAsState(initial = emptyList())
    var noteText by remember { mutableStateOf("") }
    var selectedLabelId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(noteId) {
        if (noteId != -1) {
            viewModel.loadNoteById(noteId)
        }
    }

    LaunchedEffect(selectedNote) {
        selectedNote?.let { note ->
            noteText = note.text
            selectedLabelId = note.labelId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != -1) {
                        IconButton(onClick = {
                            selectedNote?.let { note ->
                                viewModel.deleteNote(note)
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
            // Note text field
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                colors = com.horizone.pep_notes.ui.theme.pepTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata
            if (selectedNote != null) {
                Text(
                    text = "Created: ${DateFormatter.formatDateTime(selectedNote!!.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Updated: ${DateFormatter.formatDateTime(selectedNote!!.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Label selection (Max 1)
            if (allNoteLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Assign/Remove Label (Max 1)",
                    style = MaterialTheme.typography.labelMedium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allNoteLabels.forEach { label ->
                        val isSelected = label.id == selectedLabelId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLabelId = if (isSelected) null else label.id
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

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            if (noteId == -1) {
                                // Create new note - handled by parent screen
                            } else {
                                // Update existing note
                                selectedNote?.let { note ->
                                    viewModel.updateNote(
                                        note.copy(
                                            text = noteText,
                                            labelId = selectedLabelId
                                        )
                                    )
                                }
                            }
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = noteText.isNotBlank() && !isLoading
                ) {
                    Text(if (noteId == -1) "Create" else "Update")
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
