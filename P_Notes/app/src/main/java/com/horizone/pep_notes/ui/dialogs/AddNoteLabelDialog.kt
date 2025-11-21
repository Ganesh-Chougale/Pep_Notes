package com.horizone.pep_notes.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.seed.DefaultNoteLabels

@Composable
fun AddNoteLabelDialog(
    existingLabels: List<NoteLabel>,
    onDismiss: () -> Unit,
    onConfirm: (NoteLabel) -> Unit
) {
    var labelName by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (showPreview) {
        // Preview and Confirmation Dialog
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = {
                Text(
                    text = "Confirm Label",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Label Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Label Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Name: ",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f)
                        )
                        Text(
                            text = labelName,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(NoteLabel(labelName = labelName))
                        showPreview = false
                        labelName = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Label")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPreview = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }
        )
    } else {
        // Main Add Label Dialog
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Add Note Label",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Label Name Input
                    Text(
                        text = "Label Name",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = labelName,
                        onValueChange = {
                            labelName = it
                            errorMessage = ""
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

                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Preview
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                color = if (labelName.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelName.ifEmpty { "Label Preview" },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (labelName.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            labelName.isBlank() -> {
                                errorMessage = "Label name cannot be empty"
                            }
                            DefaultNoteLabels.isReservedLabelName(labelName) -> {
                                errorMessage = "This label name is reserved"
                            }
                            existingLabels.any { it.labelName.equals(labelName, ignoreCase = true) } -> {
                                errorMessage = "Label name already exists"
                            }
                            else -> {
                                showPreview = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = labelName.isNotBlank()
                ) {
                    Text("Preview")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
