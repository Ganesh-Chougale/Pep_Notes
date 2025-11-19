# Pep_Notes - API Reference

## ViewModels

### PersonViewModel
```kotlin
// State
val allPersons: Flow<List<Person>>
val searchQuery: StateFlow<String>
val searchResults: StateFlow<List<Person>>
val selectedPerson: StateFlow<Person?>
val personLabels: Flow<List<PersonLabel>>
val labelsForPerson: StateFlow<List<PersonLabel>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>

// Methods
fun updateSearchQuery(query: String)
fun selectPerson(person: Person)
fun createPerson(name: String)
fun updatePerson(person: Person)
fun deletePerson(person: Person)
fun assignLabelToPerson(personId: Int, labelId: Int)
fun removeLabelFromPerson(personId: Int, labelId: Int)
fun clearError()
```

### NoteViewModel
```kotlin
// State
val notesForPerson: StateFlow<List<Note>>
val selectedNote: StateFlow<Note?>
val noteLabels: Flow<List<NoteLabel>>
val labelsForNote: StateFlow<List<NoteLabel>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>

// Methods
fun loadNotesForPerson(personId: Int)
fun selectNote(note: Note)
fun createNote(personId: Int, text: String)
fun updateNote(note: Note)
fun deleteNote(note: Note)
fun assignLabelToNote(noteId: Int, labelId: Int)
fun removeLabelFromNote(noteId: Int, labelId: Int)
fun clearError()
```

### LabelViewModel
```kotlin
// State
val personLabels: Flow<List<PersonLabel>>
val noteLabels: Flow<List<NoteLabel>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>

// Person Label Methods
fun createPersonLabel(labelName: String)
fun updatePersonLabel(label: PersonLabel)
fun deletePersonLabel(label: PersonLabel)

// Note Label Methods
fun createNoteLabel(labelName: String)
fun updateNoteLabel(label: NoteLabel)
fun deleteNoteLabel(label: NoteLabel)

fun clearError()
```

---

## Repositories

### PersonRepository
```kotlin
fun getAllPersons(): Flow<List<Person>>
fun searchPersons(query: String): Flow<List<Person>>
suspend fun getPersonById(id: Int): Person?
suspend fun insertPerson(person: Person): Long
suspend fun updatePerson(person: Person)
suspend fun deletePerson(person: Person)
fun getLabelsForPerson(personId: Int): Flow<List<PersonLabel>>
suspend fun assignLabelToPerson(personId: Int, labelId: Int)
suspend fun removeLabelFromPerson(personId: Int, labelId: Int)
```

### NoteRepository
```kotlin
fun getNotesForPerson(personId: Int): Flow<List<Note>>
fun searchNotesForPerson(personId: Int, query: String): Flow<List<Note>>
suspend fun getNoteById(id: Int): Note?
suspend fun insertNote(note: Note): Long
suspend fun updateNote(note: Note)
suspend fun deleteNote(note: Note)
fun getLabelsForNote(noteId: Int): Flow<List<NoteLabel>>
suspend fun assignLabelToNote(noteId: Int, labelId: Int)
suspend fun removeLabelFromNote(noteId: Int, labelId: Int)
```

### PersonLabelRepository
```kotlin
fun getAllLabels(): Flow<List<PersonLabel>>
suspend fun getLabelById(id: Int): PersonLabel?
suspend fun insertLabel(label: PersonLabel): Long
suspend fun updateLabel(label: PersonLabel)
suspend fun deleteLabel(label: PersonLabel)
```

### NoteLabelRepository
```kotlin
fun getAllLabels(): Flow<List<NoteLabel>>
suspend fun getLabelById(id: Int): NoteLabel?
suspend fun insertLabel(label: NoteLabel): Long
suspend fun updateLabel(label: NoteLabel)
suspend fun deleteLabel(label: NoteLabel)
```

---

## Data Models

### Person
```kotlin
data class Person(
    val id: Int = 0,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### PersonLabel
```kotlin
data class PersonLabel(
    val id: Int = 0,
    val labelName: String
)
```

### Note
```kotlin
data class Note(
    val id: Int = 0,
    val personId: Int,
    val text: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

### NoteLabel
```kotlin
data class NoteLabel(
    val id: Int = 0,
    val labelName: String
)
```

### PersonWithLabels
```kotlin
data class PersonWithLabels(
    val person: Person,
    val labels: List<PersonLabel>
)
```

### NoteWithLabels
```kotlin
data class NoteWithLabels(
    val note: Note,
    val labels: List<NoteLabel>
)
```

---

## Navigation Routes

```kotlin
NavRoutes.PeopleList.route                    // "people_list"
NavRoutes.PersonEdit.createRoute(personId)    // "person_edit/{id}"
NavRoutes.PersonNotes.createRoute(personId)   // "person_notes/{id}"
NavRoutes.NoteEdit.createRoute(noteId)        // "note_edit/{id}"
NavRoutes.PersonLabels.route                  // "person_labels"
NavRoutes.NoteLabels.route                    // "note_labels"
NavRoutes.ExportImport.route                  // "export_import"
```

---

## Utility Functions

### DateFormatter
```kotlin
fun formatDateTime(dateTime: LocalDateTime): String
// Output: "Jan 15, 2024 14:30"

fun formatDate(dateTime: LocalDateTime): String
// Output: "Jan 15, 2024"
```

### ExportImportManager
```kotlin
suspend fun exportData(): String
// Returns JSON string of all data

suspend fun importData(jsonString: String): Boolean
// Imports data from JSON

fun saveToFile(data: String, fileName: String): Boolean
// Saves to internal storage

fun readFromFile(fileName: String): String?
// Reads from internal storage
```

---

## UI Components

### LabelChip
```kotlin
@Composable
fun LabelChip(
    label: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

---

## Screens

### PeopleListScreen
```kotlin
@Composable
fun PeopleListScreen(
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel()
)
```

### PersonEditScreen
```kotlin
@Composable
fun PersonEditScreen(
    personId: Int,
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel()
)
```

### PersonNotesScreen
```kotlin
@Composable
fun PersonNotesScreen(
    personId: Int,
    navController: NavHostController,
    noteViewModel: NoteViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel()
)
```

### NoteEditScreen
```kotlin
@Composable
fun NoteEditScreen(
    noteId: Int,
    navController: NavHostController,
    viewModel: NoteViewModel = hiltViewModel()
)
```

### PersonLabelsScreen
```kotlin
@Composable
fun PersonLabelsScreen(
    navController: NavHostController,
    viewModel: LabelViewModel = hiltViewModel()
)
```

### NoteLabelsScreen
```kotlin
@Composable
fun NoteLabelsScreen(
    navController: NavHostController,
    viewModel: LabelViewModel = hiltViewModel()
)
```

### ExportImportScreen
```kotlin
@Composable
fun ExportImportScreen(navController: NavHostController)
```

---

## Database Access

### PersonDao
```kotlin
@Insert
suspend fun insertPerson(person: Person): Long

@Update
suspend fun updatePerson(person: Person)

@Delete
suspend fun deletePerson(person: Person)

@Query("SELECT * FROM persons WHERE id = :id")
suspend fun getPersonById(id: Int): Person?

@Query("SELECT * FROM persons ORDER BY createdAt DESC")
fun getAllPersons(): Flow<List<Person>>

@Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%'")
fun searchPersons(query: String): Flow<List<Person>>
```

### NoteDao
```kotlin
@Insert
suspend fun insertNote(note: Note): Long

@Update
suspend fun updateNote(note: Note)

@Delete
suspend fun deleteNote(note: Note)

@Query("SELECT * FROM notes WHERE personId = :personId ORDER BY createdAt DESC")
fun getNotesForPerson(personId: Int): Flow<List<Note>>

@Query("SELECT * FROM notes WHERE personId = :personId AND text LIKE '%' || :query || '%'")
fun searchNotesForPerson(personId: Int, query: String): Flow<List<Note>>
```

---

## Dependency Injection

All ViewModels and Repositories are automatically injected via Hilt.

```kotlin
@HiltViewModel
class PersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personLabelRepository: PersonLabelRepository
) : ViewModel()
```

---

## Error Handling

All ViewModels expose an `error` StateFlow:

```kotlin
val error: StateFlow<String?>

fun clearError() {
    _error.value = null
}
```

Usage in UI:
```kotlin
val error by viewModel.error.collectAsState()
if (error != null) {
    // Show error message
}
```

---

## State Management Pattern

All screens use this pattern:

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    // UI based on state
}
```

---

## Notes

- All database operations are **suspend functions** (async)
- All queries return **Flow** for reactive updates
- All timestamps use **LocalDateTime**
- All IDs are auto-generated by Room
- Foreign keys are enforced with CASCADE delete
- Type converters handle LocalDateTime serialization

---

For more details, see `IMPLEMENTATION_COMPLETE.md`
