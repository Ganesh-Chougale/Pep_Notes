package com.horizone.pep_notes.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ThemePickerScreen(
    currentTheme: AppTheme,
    isDark: Boolean,
    onThemeSelected: (AppTheme) -> Unit,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Choose theme",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val themes = listOf(
                AppTheme.GOTHAM,
                AppTheme.FOREST,
                AppTheme.OCEAN,
                AppTheme.DESERT
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                themes.forEach { theme ->
                    val isCurrent = theme == currentTheme

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        // Nested theme to show a small preview of this theme's palette
                        Pep_NotesTheme(appTheme = theme, darkTheme = isDark) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (isCurrent) {
                                            Text(
                                                text = "Current",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Button(onClick = { onThemeSelected(theme) }) {
                                        Text(text = if (isCurrent) "Apply again" else "Apply")
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Simple mini preview: top bar, background, primary accent
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .padding(bottom = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // top bar color
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        // Use primary as a bar
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(6.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                // Minimal representation
                                                                Surface(
                                                                    color = MaterialTheme.colorScheme.primary
                                                                ) {}
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Aa",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Button",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
