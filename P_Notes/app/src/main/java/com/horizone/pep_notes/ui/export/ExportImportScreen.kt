package com.horizone.pep_notes.ui.export

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.horizone.pep_notes.viewmodel.ExportImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    navController: NavHostController,
    onToggleTheme: () -> Unit,
    viewModel: ExportImportViewModel = hiltViewModel()
) {
    val exportStatus by viewModel.exportStatus.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val shareIntent by viewModel.shareIntent.collectAsState()
    val showConfirm by viewModel.showImportConfirmDialog.collectAsState()

    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.requestImportFromUri(uri)
        } else {
            viewModel.onFilePickerCancelled()
        }
    }

    // Share intent launcher
    LaunchedEffect(shareIntent) {
        if (shareIntent != null) {
            val chooser = Intent.createChooser(shareIntent, "Share backup file to:")
            try {
                navController.context.startActivity(chooser)
            } catch (e: Exception) {
                // Handle error
            }
            viewModel.clearShareIntent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Toggle theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelImport() },
                title = { Text("Confirm Import") },
                text = { Text("Importing will replace all current data. Continue?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmImport() }) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelImport() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Export Section
            ExportSection(
                isExporting = isExporting,
                exportStatus = exportStatus,
                exportProgress = exportProgress,
                onExportToApps = { viewModel.exportDataToApps() },
                onExportLocal = { viewModel.exportData() },
                buttonsEnabled = !(isExporting || isImporting)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Import Section
            ImportSection(
                isImporting = isImporting,
                importStatus = importStatus,
                importProgress = importProgress,
                onImportFromFile = { filePickerLauncher.launch("application/json") },
                buttonsEnabled = !(isExporting || isImporting)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Box
            InfoBox()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExportSection(
    isExporting: Boolean,
    exportStatus: String,
    exportProgress: String,
    onExportToApps: () -> Unit,
    onExportLocal: () -> Unit,
    buttonsEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export data",
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Export Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose how to export your data:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Export to Apps Button
        Button(
            onClick = onExportToApps,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = buttonsEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Share to Apps")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Export Local Button
        Button(
            onClick = onExportLocal,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = buttonsEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Save Locally")
        }

        if (exportProgress.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exportProgress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (exportStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StatusMessage(
                message = exportStatus,
                isSuccess = exportStatus.contains("✓")
            )
        }
    }
}

@Composable
private fun ImportSection(
    isImporting: Boolean,
    importStatus: String,
    importProgress: String,
    onImportFromFile: () -> Unit,
    buttonsEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Import data",
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Import Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select a JSON backup file from your device:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onImportFromFile,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = buttonsEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Import data",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Choose File")
        }

        if (importProgress.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = importProgress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (importStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StatusMessage(
                message = importStatus,
                isSuccess = importStatus.contains("✓")
            )
        }
    }
}

@Composable
private fun StatusMessage(message: String, isSuccess: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSuccess)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
            .padding(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSuccess)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun InfoBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "ℹ️ Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Export: Save your data as JSON and share via WhatsApp, Google Drive, Email, etc.\n" +
                        "• Import: Restore data from a previously exported JSON file\n" +
                        "• File Format: Only valid .json backup files are accepted\n" +
                        "• Location: Exports are saved to your device's Documents folder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
