package com.horizone.pep_notes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horizone.pep_notes.data.db.PepDatabase
import com.horizone.pep_notes.util.ExportImportManager
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
    
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting
    
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting

    fun exportData() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportStatus.value = "Exporting..."
                
                val jsonData = exportImportManager.exportData()
                val saved = exportImportManager.saveToFile(jsonData)
                
                if (saved) {
                    _exportStatus.value = "✓ Export successful!"
                } else {
                    _exportStatus.value = "✗ Failed to save file"
                }
            } catch (e: Exception) {
                _exportStatus.value = "✗ Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun importData() {
        viewModelScope.launch {
            try {
                _isImporting.value = true
                _importStatus.value = "Importing..."
                
                val jsonData = exportImportManager.readFromFile("pep_notes_backup.json")
                if (jsonData == null) {
                    _importStatus.value = "✗ No backup file found"
                    return@launch
                }
                
                val success = exportImportManager.importData(jsonData)
                if (success) {
                    _importStatus.value = "✓ Import successful!"
                } else {
                    _importStatus.value = "✗ Import failed"
                }
            } catch (e: Exception) {
                _importStatus.value = "✗ Import failed: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun clearStatus() {
        _exportStatus.value = ""
        _importStatus.value = ""
    }
}
