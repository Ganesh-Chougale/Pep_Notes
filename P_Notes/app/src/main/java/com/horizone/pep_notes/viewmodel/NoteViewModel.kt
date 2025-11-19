package com.horizone.pep_notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.repository.NoteRepository
import com.horizone.pep_notes.data.repository.NoteLabelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val noteLabelRepository: NoteLabelRepository
) : ViewModel() {

    // Notes for selected person
    private val _notesForPerson = MutableStateFlow<List<Note>>(emptyList())
    val notesForPerson = _notesForPerson.asStateFlow()

    // Selected note
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote = _selectedNote.asStateFlow()

    // Note labels
    private val _noteLabels = noteLabelRepository.getAllLabels()
    val noteLabels = _noteLabels

    // Labels for selected note
    private val _labelsForNote = MutableStateFlow<List<NoteLabel>>(emptyList())
    val labelsForNote = _labelsForNote.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadNotesForPerson(personId: Int) {
        viewModelScope.launch {
            noteRepository.getNotesForPerson(personId).collect { notes ->
                _notesForPerson.value = notes
            }
        }
    }

    fun selectNote(note: Note) {
        _selectedNote.value = note
        viewModelScope.launch {
            noteRepository.getLabelsForNote(note.id).collect { labels ->
                _labelsForNote.value = labels
            }
        }
    }

    fun createNote(personId: Int, text: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val note = Note(
                    personId = personId,
                    text = text,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                noteRepository.insertNote(note)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedNote = note.copy(updatedAt = LocalDateTime.now())
                noteRepository.updateNote(updatedNote)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.deleteNote(note)
                _selectedNote.value = null
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun assignLabelToNote(noteId: Int, labelId: Int) {
        viewModelScope.launch {
            try {
                noteRepository.assignLabelToNote(noteId, labelId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeLabelFromNote(noteId: Int, labelId: Int) {
        viewModelScope.launch {
            try {
                noteRepository.removeLabelFromNote(noteId, labelId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
