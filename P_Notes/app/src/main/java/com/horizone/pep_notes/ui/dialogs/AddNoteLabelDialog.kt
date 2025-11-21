package com.horizone.pep_notes.ui.dialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*

import com.horizone.pep_notes.data.model.*
import com.horizone.pep_notes.data.seed.*

@Composable
fun AddNoteLabelDialog(
    existingLabels: List<NoteLabel>,
    onDismiss: () -> Unit,
    onConfirm: (NoteLabel) -> Unit
) {
    val allColors = listOf(
        "#2196F3", // Blue
        "#F44336", // Red
        "#FFC107", // Yellow
        "#4CAF50", // Green
        "#FF9800", // Orange
        "#9C27B0"  // Purple
    )

    // Get used colors from existing labels and default labels
    val usedColors = existingLabels.map { it.colorCode }.toSet() + DefaultNoteLabels.getDefaultColorCodes()
    
    // Filter to show only available colors
    val availableColors = allColors.filter { it !in usedColors }
    
    var labelName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "#2196F3") }
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
                                color = Color(android.graphics.Color.parseColor(selectedColor)),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelName,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Color: ",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f)
                        )
                        Text(
                            text = selectedColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(NoteLabel(labelName = labelName, colorCode = selectedColor))
                        showPreview = false
                        labelName = ""
                        selectedColor = "#2196F3"
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

                    // Color Picker
                    Text(
                        text = "Select Color",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = Color(android.graphics.Color.parseColor(color)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedColor = color }
                                    .then(
                                        if (selectedColor == color) {
                                            Modifier
                                                .border(
                                                    width = 3.dp,
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }

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
                                color = if (labelName.isNotEmpty()) Color(android.graphics.Color.parseColor(selectedColor)) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelName.ifEmpty { "Label Preview" },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (labelName.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
                            DefaultNoteLabels.isReservedColorCode(selectedColor) -> {
                                errorMessage = "This color is reserved"
                            }
                            existingLabels.any { it.colorCode == selectedColor } -> {
                                errorMessage = "Color already used"
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
