# 🚀 **PHASE 1 — Project Setup (Clean Foundation)**

### ✅ Steps

1. Open Android Studio → New Project → “Empty Activity”
2. Enable:

   * Kotlin
   * Jetpack Compose
   * Material 3
3. Create project package structure RIGHT NOW (Separation of Concerns):

```
com.pepnote
   ├─ data/
   │    ├─ model/
   │    ├─ db/
   │    └─ repository/
   ├─ ui/
   │    ├─ people/
   │    ├─ notes/
   │    ├─ labels/
   │    ├─ export/
   │    └─ nav/
   ├─ viewmodel/
   └─ util/
```

4. Keep **MainActivity simple** (SRP: only sets UI + NavHost).

---

# 🚧 **PHASE 2 — Navigation Architecture**

This is the “app skeleton”.

### Steps

1. Create `AppNavHost.kt` under `ui/nav/`
2. Add all screens as routes:

```
people_list
person_edit/{id}
person_notes/{id}
note_edit/{id}
person_labels
note_labels
export_import
```

3. Leave screens empty for now — just placeholders.

### Why?

* Decomposition: screens separate
* SRP: NavHost handles ONLY navigation
* Modularization: UI parts decoupled

---

# 🧱 **PHASE 3 — Data Layer (Room + Models)**

Implement the core DB.

### Steps

1. Under `data/model`, create:

   * Person
   * PersonLabel
   * Note
   * NoteLabel

2. Under `data/db`, create:

   * `PersonDao`
   * `LabelDao`
   * `NoteDao`
   * `PepDatabase`

3. Under `data/repository`:

   * PersonRepository
   * NoteRepository
   * LabelRepository

### Why?

* SRP → database separate from UI
* Extraction → each entity in its own file
* Modularization → repository is middle layer

---

# 🤖 **PHASE 4 — ViewModels (Business Logic Layer)**

### Steps

1. In `viewmodel/` create:

   * PersonViewModel
   * NoteViewModel
   * LabelViewModel

2. Each ViewModel:

   * Talks to repository
   * Handles state
   * Handles CRUD

### Why?

* Separation of Concerns → UI no longer does logic
* SRP → ViewModel = single responsibility: business logic for a screen group

---

# 🎨 **PHASE 5 — Build Screens One by One (UI Layer)**

Build small → test → next.

### Screen Order:

1. **PeopleListScreen**

   * Show list
   * Search
   * Add person → navigate

2. **PersonEditScreen**

   * Name input
   * Assign labels
   * Save

3. **PersonNotesScreen**

   * List of notes for the selected person
   * Add note

4. **NoteEditScreen**

   * Add/edit note
   * Assign note labels

5. **Label screens**

   * PersonLabelManager
   * NoteLabelManager

6. **ExportImportScreen**

### Why this order?

* Decomposition → build smallest steps first
* SRP → each screen does ONE clear job

---

# 💾 **PHASE 6 — Export/Import (Backup)**

### Steps

1. Create a utility class in `/util/export_import/`
2. Export:

   * Query all tables
   * Convert to JSON
   * Write to file using Storage Access Framework
3. Import:

   * Read JSON
   * Replace DB tables

### Why?

* Separation of Concerns → utility does only JSON & file ops
* Modular → no UI logic inside utilities

---

# 🧹 **PHASE 7 — Refactoring & Cleanup**

Apply your 5 rules:

### 1. **Extract Class**

Move repeated logic from screens → separate classes
Example: label chip components, timestamp formatter.

### 2. **SRP Refactoring**

Check if any class does too much—split it.
Example: If `PersonViewModel` also handles labels → separate it.

### 3. **Decomposition**

Split large composables into small reusable UI parts.
Example: Reusable TextFieldWithLabel, NoteCard, LabelChip.

### 4. **Separation of Concerns**

Check:

* UI doesn’t know database
* ViewModel doesn’t know UI
* DB doesn’t know UI or ViewModel

### 5. **Modularization**

(Optional advanced step later):

* Move data layer into a separate Gradle module
* Move UI into feature-based modules

---

# 🧪 **PHASE 8 — Testing (Basic Functional Testing)**

* Test adding/editing people
* Test note history
* Test labels
* Test export/import

No need deep unit tests for now.

---

# 🏁 **PHASE 9 — Polish & Publish**

* Add Material 3 theming
* Add icons
* Add backups
* Create release build
* Sign APK

---

# ✔️ Final Summary (Quick List)

### **PHASES**

1. Setup project
2. Navigation skeleton
3. Data layer (Room + models)
4. ViewModels
5. Build screens (UI)
6. Export/Import feature
7. Refactor with SRP + decomposition
8. Test
9. Polish & release

This roadmap will get you there without chaos.

---
