package com.horizone.pep_notes.ui.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.horizone.pep_notes.viewmodel.ExportImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    navController: NavHostController,
    viewModel: ExportImportViewModel = hiltViewModel()
) {
    val exportStatus by viewModel.exportStatus.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export/Import") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Export section
            Button(
                onClick = { viewModel.exportData() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting
            ) {
                Text(if (isExporting) "Exporting..." else "Export Data")
            }

            if (exportStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exportStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (exportStatus.contains("✓")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Import section
            Button(
                onClick = { viewModel.importData() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                Text(if (isImporting) "Importing..." else "Import Data")
            }

            if (importStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = importStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (importStatus.contains("✓")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Note: Export saves your data as JSON. Import restores from a JSON file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
