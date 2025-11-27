package com.horizone.pep_notes.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horizone.pep_notes.data.db.PepDatabase
import com.horizone.pep_notes.util.ExportImportManager
import com.horizone.pep_notes.util.FileReadResult
import com.horizone.pep_notes.util.FileWriteResult
import com.horizone.pep_notes.util.IoErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PepDatabase
) : ViewModel() {
    
    private val exportImportManager = ExportImportManager(context, database)
    
    private val _exportStatus = MutableStateFlow("")
    val exportStatus: StateFlow<String> = _exportStatus
    
    private val _importStatus = MutableStateFlow("")
    val importStatus: StateFlow<String> = _importStatus

    private val _exportProgress = MutableStateFlow("")
    val exportProgress: StateFlow<String> = _exportProgress

    private val _importProgress = MutableStateFlow("")
    val importProgress: StateFlow<String> = _importProgress
    
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting
    
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent

    private val _showImportConfirmDialog = MutableStateFlow(false)
    val showImportConfirmDialog: StateFlow<Boolean> = _showImportConfirmDialog

    private var pendingImportUri: Uri? = null

    fun exportData() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportProgress.value = "Exporting..."
                
                when (val result = exportImportManager.exportToInternalStorageStreaming()) {
                    is FileWriteResult.Success -> {
                        _exportStatus.value = "✓ Export successful!"
                    }
                    is FileWriteResult.Error -> {
                        _exportStatus.value = when (result.error) {
                            IoErrorType.PERMISSION_DENIED -> "✗ Export failed: Permission denied"
                            IoErrorType.NO_SPACE -> "✗ Export failed: Not enough storage space"
                            IoErrorType.FILESYSTEM_UNAVAILABLE -> "✗ Export failed: Storage unavailable"
                            IoErrorType.FILE_NOT_FOUND -> "✗ Export failed: Destination not found"
                            IoErrorType.WRITE_ERROR, IoErrorType.UNKNOWN -> "✗ Export failed: Failed to save file"
                            else -> "✗ Export failed: Failed to save file"
                        }
                    }
                }
            } catch (e: Exception) {
                _exportStatus.value = "✗ Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
                _exportProgress.value = "Done"
            }
        }
    }

    fun importData() {
        viewModelScope.launch {
            try {
                _isImporting.value = true
                _importProgress.value = "Reading file..."
                
                val jsonData = exportImportManager.readFromFile("pep_notes_backup.json")
                if (jsonData == null) {
                    _importStatus.value = "✗ No backup file found"
                    return@launch
                }

                // Validate backup file format for local file as well
                if (!exportImportManager.isValidBackupFile(jsonData)) {
                    _importStatus.value = "✗ Invalid backup file format"
                    return@launch
                }

                _importProgress.value = "Importing..."
                val result = exportImportManager.importData(jsonData)
                _importStatus.value = result.message
            } catch (e: Exception) {
                _importStatus.value = "✗ Import failed: ${e.message}"
            } finally {
                _isImporting.value = false
                _importProgress.value = "Done"
            }
        }
    }

    fun requestImportFromLocal() {
        pendingImportUri = null
        _showImportConfirmDialog.value = true
    }

    fun clearStatus() {
        _exportStatus.value = ""
        _importStatus.value = ""
        _exportProgress.value = ""
        _importProgress.value = ""
    }

    /**
     * Export data and prepare share intent for native apps
     */
    fun exportDataToApps() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportProgress.value = "Preparing export..."
                
                // Save to external storage via streaming
                when (val result = exportImportManager.exportToExternalStorageStreaming()) {
                    is FileWriteResult.Success -> {
                        val fileUri = result.uri
                        if (fileUri != null) {
                            _shareIntent.value = exportImportManager.getShareIntent(fileUri)
                            _exportStatus.value = "✓ Ready to share! Select an app..."
                        } else {
                            _exportStatus.value = "✗ Failed to save file"
                        }
                    }
                    is FileWriteResult.Error -> {
                        _exportStatus.value = when (result.error) {
                            IoErrorType.PERMISSION_DENIED -> "✗ Export failed: Permission denied"
                            IoErrorType.NO_SPACE -> "✗ Export failed: Not enough storage space"
                            IoErrorType.FILESYSTEM_UNAVAILABLE -> "✗ Export failed: Storage unavailable"
                            IoErrorType.FILE_NOT_FOUND -> "✗ Export failed: Destination not found"
                            IoErrorType.WRITE_ERROR, IoErrorType.UNKNOWN -> "✗ Export failed: Failed to save file"
                            else -> "✗ Export failed: Failed to save file"
                        }
                    }
                }
            } catch (e: Exception) {
                _exportStatus.value = "✗ Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
                _exportProgress.value = "Done"
            }
        }
    }

    /**
     * Import data from selected file URI
     */
    fun importDataFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                _isImporting.value = true
                _importProgress.value = "Reading file..."

                when (val read = exportImportManager.readFromUriResult(uri)) {
                    is FileReadResult.Success -> {
                        val jsonData = read.data
                        // Validate backup file format
                        if (!exportImportManager.isValidBackupFile(jsonData)) {
                            _importStatus.value = "✗ Invalid backup file format"
                            return@launch
                        }

                        _importProgress.value = "Importing..."
                        val result = exportImportManager.importData(jsonData)
                        _importStatus.value = result.message
                    }
                    is FileReadResult.Error -> {
                        _importStatus.value = when (read.error) {
                            IoErrorType.PERMISSION_DENIED -> "✗ Permission denied to read file"
                            IoErrorType.FILE_NOT_FOUND, IoErrorType.INVALID_URI -> "✗ File not found"
                            IoErrorType.READ_ERROR, IoErrorType.UNKNOWN -> "✗ Failed to read file"
                            else -> "✗ Failed to read file"
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                _importStatus.value = "✗ Import failed: ${e.message}"
            } finally {
                _isImporting.value = false
                _importProgress.value = "Done"
            }
        }
    }

    fun requestImportFromUri(uri: Uri) {
        pendingImportUri = uri
        _showImportConfirmDialog.value = true
    }

    fun confirmImport() {
        val uri = pendingImportUri
        _showImportConfirmDialog.value = false
        pendingImportUri = null
        if (uri != null) {
            importDataFromUri(uri)
        } else {
            importData()
        }
    }

    fun cancelImport() {
        _showImportConfirmDialog.value = false
        pendingImportUri = null
        _isImporting.value = false
        _importStatus.value = "Import cancelled"
        _importProgress.value = ""
    }

    fun onFilePickerCancelled() {
        _isImporting.value = false
        _importStatus.value = "Import cancelled"
        _importProgress.value = ""
    }

    /**
     * Clear share intent after use
     */
    fun clearShareIntent() {
        _shareIntent.value = null
    }
}
