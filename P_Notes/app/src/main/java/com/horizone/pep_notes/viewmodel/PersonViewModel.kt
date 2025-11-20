package com.horizone.pep_notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.repository.PersonRepository
import com.horizone.pep_notes.data.repository.PersonLabelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personLabelRepository: PersonLabelRepository
) : ViewModel() {

    // People list
    private val _allPersons = personRepository.getAllPersons()
    val allPersons = _allPersons

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Person>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Selected person
    private val _selectedPerson = MutableStateFlow<Person?>(null)
    val selectedPerson = _selectedPerson.asStateFlow()

    // Person labels
    private val _personLabels = personLabelRepository.getAllLabels()
    val personLabels = _personLabels

    // Labels for selected person
    private val _labelsForPerson = MutableStateFlow<List<PersonLabel>>(emptyList())
    val labelsForPerson = _labelsForPerson.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
        } else {
            viewModelScope.launch {
                personRepository.searchPersons(query).collect { results ->
                    _searchResults.value = results
                }
            }
        }
    }

    fun selectPerson(person: Person) {
        _selectedPerson.value = person
        viewModelScope.launch {
            personRepository.getLabelsForPerson(person.id).collect { labels ->
                _labelsForPerson.value = labels
            }
        }
    }

    fun loadPersonById(personId: Int) {
        viewModelScope.launch {
            try {
                val person = personRepository.getPersonById(personId)
                if (person != null) {
                    _selectedPerson.value = person
                    personRepository.getLabelsForPerson(person.id).collect { labels ->
                        _labelsForPerson.value = labels
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createPerson(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val person = Person(name = name)
                personRepository.insertPerson(person)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                personRepository.updatePerson(person)
                _selectedPerson.value = person
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                personRepository.deletePerson(person)
                _selectedPerson.value = null
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun assignLabelToPerson(personId: Int, labelId: Int) {
        viewModelScope.launch {
            try {
                personRepository.assignLabelToPerson(personId, labelId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeLabelFromPerson(personId: Int, labelId: Int) {
        viewModelScope.launch {
            try {
                personRepository.removeLabelFromPerson(personId, labelId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
