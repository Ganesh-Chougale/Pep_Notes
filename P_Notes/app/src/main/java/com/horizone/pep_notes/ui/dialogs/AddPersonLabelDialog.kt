package com.horizone.pep_notes.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.seed.DefaultPersonLabels

@Composable
fun AddPersonLabelDialog(
    existingLabels: List<PersonLabel>,
    onDismiss: () -> Unit,
    onConfirm: (PersonLabel) -> Unit
) {
    val allColors = listOf(
        "#8B5CF6", // Purple - neutral, works with light/dark themes
        "#EC4899", // Pink - vibrant but not too bright
        "#06B6D4", // Cyan - cool tone, theme-safe
        "#F59E0B", // Amber - warm tone, accessible
        "#10B981"  // Emerald - natural green, theme-safe
    )

    // Get used colors from existing labels and default labels
    val usedColors = existingLabels.map { it.colorCode }.toSet() + DefaultPersonLabels.getDefaultColorCodes()
    
    // Filter to show only available colors
    val availableColors = allColors.filter { it !in usedColors }
    
    var labelName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "#8B5CF6") }
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
                        onConfirm(PersonLabel(labelName = labelName, colorCode = selectedColor))
                        showPreview = false
                        labelName = ""
                        selectedColor = "#FF6B6B"
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
                    text = "Add Person Label",
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
                            DefaultPersonLabels.isReservedLabelName(labelName) -> {
                                errorMessage = "This label name is reserved"
                            }
                            existingLabels.any { it.labelName.equals(labelName, ignoreCase = true) } -> {
                                errorMessage = "Label name already exists"
                            }
                            DefaultPersonLabels.isReservedColorCode(selectedColor) -> {
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
