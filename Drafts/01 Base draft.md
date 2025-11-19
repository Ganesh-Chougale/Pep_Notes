i want to create a simple android app, a note taking app
- we can create/edit/delete a person
- we can create/edit/delete a person's label
- we can create/edit/delete a note
- we can create/edit/delete a note's label
- we can choose a person from created people
- we will add note to that person, it will automatically register with timestap
- we can label the person with person label & we can label the note with note label
- we can create label, use them the person
- everything will be stored in local data & entire data can be export to use it later like app uninstalled, app data wiped, or changing handset

Alright Ganesh, here’s a clean, practical blueprint for your **Pep_Note** app—simple enough to build, solid enough to actually ship.

---

## 🧩 Core Data Model (Minimum Needed)

Think of these as your tables if you use Room DB.

### **1. Person**

* `id`
* `name`
* `labels` (list of PersonLabel IDs)
* `createdAt`

### **2. PersonLabel**

* `id`
* `labelName`

### **3. Note**

* `id`
* `personId`
* `text`
* `labels` (list of NoteLabel IDs)
* `createdAt`
* `updatedAt`

### **4. NoteLabel**

* `id`
* `labelName`

Everything is tied through `id`s → neat, scalable.

---

## 🛠️ Features (Mapped to UI Screens)

### **1. Home Screen**

* List of all **people**
* Button: “Add Person”
* Tap person → open that person’s notes

### **2. Person Editor**

* Add/Edit name
* Add/remove labels (chip-style)
* Delete person

### **3. Person Labels Manager**

* Add/Edit/Delete person labels
* Use labels as chips when assigning

### **4. Notes Screen (for a selected person)**

* Show all notes for that person
* Each note shows:

  * Timestamp
  * Preview text
  * Labels (chips)

### **5. Note Editor**

* Add/Edit/Delete note
* Auto timestamp on create
* Update timestamp on edit
* Assign labels to note

### **6. Note Labels Manager**

* Add/Edit/Delete note labels

---

## 💾 Storage Strategy (Local + Export)

Use **Room** as the main local database.

For export/import:

### Export:

* Convert all tables → JSON
* Save to:

  * Internal storage
  * Or user-selected folder (using Storage Access Framework)

### Import:

* Pick a JSON file
* Replace local DB (or merge if you want)

JSON structure:

```
{
  "persons": [...],
  "personLabels": [...],
  "notes": [...],
  "noteLabels": [...]
}
```

Super clean, super future-proof.

---

## 🔄 Backup/Restore Flow

* **Backup**: user taps `Export → Save JSON`
* **Restore**: user taps `Import → Pick JSON → App rebuilds DB`

Works even after reinstall or switching phone.


# 📄 **PEP_NOTE — ACTIVITY SHEET**

## **1️⃣ MainActivity (ONLY ACTIVITY)**

**Purpose:**
Just hosts the entire app UI + NavGraph.
No business logic.
No big code.

**Inside MainActivity:**

* `setContent { AppNavHost() }`

**Lines of code:**
~20–40 max.

---

# 📄 **SCREENS (Composables inside MainActivity)**

*(Each one lives in its own .kt file)*

## **2️⃣ PeopleListScreen**

**Shows:**

* All people
* Search bar
* Add person button
  **Action:**
  Tap → opens PersonNotesScreen

---

## **3️⃣ PersonEditScreen**

**Allows:**

* Create/edit person
* Assign/remove person labels
* Delete person

---

## **4️⃣ PersonNotesScreen (HISTORY PAGE)**

**This is the screen user sees after selecting a person**
Shows:

* All notes for the selected person
* Timestamps
* Note labels
* Add note button
  Tap note → NoteEditScreen

---

## **5️⃣ NoteEditScreen**

**Allows:**

* Add/edit note
* Add labels
* Delete note

---

## **6️⃣ PersonLabelManagerScreen**

**Allows:**

* Add/edit/delete person labels
* Used inside PersonEditScreen

---

## **7️⃣ NoteLabelManagerScreen**

**Allows:**

* Add/edit/delete note labels
* Used inside NoteEditScreen

---

## **8️⃣ ExportImportScreen**

**Allows:**

* Export entire DB to JSON
* Import JSON to restore data

---

# 🧭 **Navigation Sheet (Inside NavHost)**

These are your routes:

```
people_list
person_edit/{personId}
person_notes/{personId}
note_edit/{noteId}
person_labels
note_labels
export_import
```

---

# 🏗️ **Project Folder Structure**

```
/ui
   /people
   /notes
   /labels
   AppNavHost.kt
   MainActivity.kt

/viewmodel
   PersonViewModel.kt
   NoteViewModel.kt
   LabelViewModel.kt

/data
   /db (Room)
   /repository
   /model

/utils
   /export_import
```