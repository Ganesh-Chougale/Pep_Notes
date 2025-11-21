package com.horizone.pep_notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.repository.NoteLabelRepository
import com.horizone.pep_notes.data.repository.PersonLabelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelViewModel @Inject constructor(
    private val personLabelRepository: PersonLabelRepository,
    private val noteLabelRepository: NoteLabelRepository
) : ViewModel() {

    // Person labels
    private val _personLabels = personLabelRepository.getAllLabels()
    val personLabels = _personLabels
    val allPersonLabels = _personLabels

    // Note labels
    private val _noteLabels = noteLabelRepository.getAllLabels()
    val noteLabels = _noteLabels

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Person label operations
    fun createPersonLabel(labelName: String, colorCode: String = "#FF6B6B") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val label = PersonLabel(labelName = labelName, colorCode = colorCode)
                personLabelRepository.insertLabel(label)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePersonLabel(label: PersonLabel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                personLabelRepository.updateLabel(label)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePersonLabel(label: PersonLabel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                personLabelRepository.deleteLabel(label)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Note label operations
    fun createNoteLabel(labelName: String, colorCode: String = "#808080") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val label = NoteLabel(labelName = labelName, colorCode = colorCode)
                noteLabelRepository.insertLabel(label)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNoteLabel(label: NoteLabel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteLabelRepository.updateLabel(label)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNoteLabel(label: NoteLabel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteLabelRepository.deleteLabel(label)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
