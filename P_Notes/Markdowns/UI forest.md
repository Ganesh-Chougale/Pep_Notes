> Goal: Add a forest/woods Day & Night theme with a global toggle, in small, safe steps.  
> Each phase is self‑contained; later phases build on earlier ones without “breaking” them.
> Each Icon used will have accurate import, no error permissible here
> One phase at a time only

# A. unFinished phases & steps: 

## Phase 1 – Theme Foundation (Add Forest Colors, No Wiring Yet)

- **[Step 1.1] Add forest colors**
  - In [Color.kt](cci:7://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Color.kt:0:0-0:0), define color constants (names like `ForestPrimaryLight`, `ForestPrimaryDark`, etc.) for:
    - Day: greens + warm browns, light backgrounds.
    - Night: deep greens, dark surfaces, high contrast text.
  - Use hex values decided in Phase 0.

- **[Step 1.2] Define forest color schemes**
  - In [Theme.kt](cci:7://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:0:0-0:0), create:
    - `ForestLightColorScheme = lightColorScheme(...)`
    - `ForestDarkColorScheme = darkColorScheme(...)`
  - Map your new color constants into `primary`, `secondary`, `background`, `surface`, `surfaceVariant`, etc.

- **[Step 1.3] Keep current behavior**
  - Do **not** change how [Pep_NotesTheme](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1) currently chooses `colorScheme`.
  - App should still look exactly the same (purple/dynamic) after this phase.

- **Exit criteria**
  - Project builds.
  - No visual changes yet.
  - Forest color schemes exist and are ready to be used.

---

## Phase 2 – Forest Theme Integration (Replace Colors, Still No Toggle)

- **[Step 2.1] Update [Pep_NotesTheme](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1) to use forest schemes**
  - In [Pep_NotesTheme](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1), switch from the old `DarkColorScheme`/`LightColorScheme` to the new `ForestDarkColorScheme` / `ForestLightColorScheme`.
  - Decide what to do with `dynamicColor`:
    - **Option A (simple)**: Ignore/disable dynamic colors and always use forest.
    - **Option B**: Keep dynamic colors for users who enable a “System theme” option later.

- **[Step 2.2] Keep system dark mode mapping**
  - For now, keep:
    - `darkTheme: Boolean = isSystemInDarkTheme()`
  - Use `darkTheme` only to choose **forest dark** vs **forest light**.

- **[Step 2.3] Visual QA**
  - Run the app in light and dark mode:
    - Check top bars, cards (`surfaceVariant`), dialogs, FABs, etc.
  - Adjust specific color slots if text contrast is poor.

- **Exit criteria**
  - App now uses forest palette for light & dark.
  - Switching system dark mode on/off correctly switches between forest day/night.
  - No crashes; only visual changes vs previous phase.

---

## Phase 3 – Introduce Explicit Theme State at Root (Plumbing Only)

- **[Step 3.1] Add theme state to [MainActivity](cci:2://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:15:0-28:1)**
  - In `setContent { ... }`, create state:
    - `isForestDark: Boolean`
  - Initialize it:
    - Either from `isSystemInDarkTheme()` or (later) from stored preference.

- **[Step 3.2] Connect [Pep_NotesTheme](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1) to explicit state**
  - Call:
    - [Pep_NotesTheme(darkTheme = isForestDark) { ... }](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1)
  - Internally, [Pep_NotesTheme](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1) now **only** trusts the boolean you pass.
  - System dark mode may still be used *once* to set the *initial* value.

- **[Step 3.3] Thread state & handler downward**
  - Define a lambda in [MainActivity](cci:2://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:15:0-28:1):
    - `onToggleTheme: () -> Unit` that flips `isForestDark`.
  - Update:
    - [AppContent(isForestDark, onToggleTheme)](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:30:0-34:1)
    - `AppNavHost(navController, isForestDark, onToggleTheme)`
  - **Do not** add any UI controls yet; just pass the handler through.

- **[Step 3.4] Verify stability**
  - Temporarily call `onToggleTheme` from inside `setContent` or a small test button to ensure:
    - No recomposition crashes.
    - Whole app theme updates correctly.

- **Exit criteria**
  - Theme is controlled exclusively by root state.
  - State and handler are successfully threaded: [MainActivity](cci:2://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:15:0-28:1) → [AppContent](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:30:0-34:1) → `AppNavHost` → screens.
  - No user-visible toggle yet.

---

## Phase 4 – Add Primary Theme Toggle UI (PeopleListScreen Only)

- **[Step 4.1] Update [PeopleListScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/people/PeopleListScreen.kt:25:0-167:1) API**
  - Add a new parameter:
    - `onToggleTheme: () -> Unit`
  - Update `AppNavHost`:
    - Route for [PeopleListScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/people/PeopleListScreen.kt:25:0-167:1) calls [PeopleListScreen(..., onToggleTheme = onToggleTheme)](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/people/PeopleListScreen.kt:25:0-167:1).

- **[Step 4.2] Place the toggle control in UI**
  - In the top `Row` where you currently have:
    - Search `TextField`
    - Menu `IconButton` (MoreVert)
  - Add a forest Day/Night icon `IconButton`:
    - Position: before or after the menu button.
    - Call `onToggleTheme()` on click.
    - Use appropriate content description for accessibility (“Toggle theme” or similar).

- **[Step 4.3] Basic UX behavior**
  - Toggle should switch `isForestDark` and thus forest light ↔ dark instantly.
  - No persistence yet; theme resets when app restarts.

- **[Step 4.4] Regression tests**
  - Check [PeopleListScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/people/PeopleListScreen.kt:25:0-167:1) behaviors: search, menu dialogs, list scroll, FAB.
  - Ensure clicking the theme icon doesn’t conflict with other state.

- **Exit criteria**
  - User can toggle forest Day/Night from the main screen.
  - No other screens are touched visually beyond the theme change itself.

---

## Phase 5 – Optional Secondary Toggles in Other Screens

*(Only do this if you want access everywhere; it doesn’t break previous work.)*

- **[Step 5.1] Extend screen signatures**
  - For each of these screens:
    - [PersonNotesScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/notes/PersonNotesScreen.kt:25:0-173:1)
    - [NoteEditScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/notes/NoteEditScreen.kt:44:0-164:1)
    - [PersonLabelsScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/labels/PersonLabelsScreen.kt:44:0-120:1)
    - [NoteLabelsScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/labels/NoteLabelsScreen.kt:46:0-123:1)
    - [ExportImportScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/export/ExportImportScreen.kt:47:0-159:1)
  - Add `onToggleTheme: () -> Unit` parameter.

- **[Step 5.2] Wire through `AppNavHost`**
  - Where each route composable is created, pass `onToggleTheme` through.

- **[Step 5.3] Add an action icon to `TopAppBar`**
  - In each screen’s `TopAppBar`, in `actions { ... }`:
    - Add an `IconButton` with the same theme icon and behavior (calls `onToggleTheme()`).
  - Keep layout consistent across screens.

- **[Step 5.4] Visual & navigation checks**
  - Ensure back navigation still works.
  - Confirm the theme toggler doesn’t overlap with other actions.

- **Exit criteria**
  - All chosen screens have a working theme toggle in their top bar.
  - Behavior is identical to the main screen toggle (shared root state).

---

## Phase 6 – Persistence & Polish (Safe Enhancements)

- **[Step 6.1] Implement persistence**
  - Choose storage:
    - DataStore (recommended) or SharedPreferences.
  - Introduce a `ThemeViewModel` or a small repo used by [MainActivity](cci:2://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:15:0-28:1):
    - Load saved `isForestDark` on startup.
    - Save whenever `onToggleTheme()` changes the value.

- **[Step 6.2] Smooth transitions (optional)**
  - Add light animations / crossfade when theme changes:
    - e.g. use `AnimatedContent` or simple alpha fades around major surfaces.

- **[Step 6.3] Cleanup**
  - Remove unused old purple color constants and schemes if you don’t need them.
  - Remove or simplify `dynamicColor` branches if you’ve decided not to use dynamic colors.

- **[Step 6.4] Final QA**
  - Test:
    - Fresh install, first launch in system light mode and dark mode.
    - App restart after setting Day / Night manually.
    - All major screens verifying contrasts and readability.

- **Exit criteria**
  - Forest Day/Night theme is stable, persists across launches.
  - UX is consistent; no dead/unreachable code related to theming.

---

# B. Finished phases & steps: 

## Phase 0 – Requirements & Design Decisions (No Code)
- **[Step 0.1] Define behavior**
  - Decide: Should theme respect system dark mode as a *default* for first launch? 
	Answer: No, keep it day 7 user will decide to change it.
  - Decide: Should user choice override system and be **persistent** (recommended)?
	Answer: Yes.
  - Decide: Keep Android 12+ dynamic colors or **always** use custom forest palette?
	Answer: whatever ideal is, choose it.
	
- **[Step 0.2] UX decisions**
  - Confirm primary toggle location:
    - [PeopleListScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/people/PeopleListScreen.kt:25:0-167:1) top row (beside search & menu) as **main toggle**.
	Answer: Yes.
  - Decide if you also want secondary toggles:
    - In `TopAppBar` actions of: [PersonNotesScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/notes/PersonNotesScreen.kt:25:0-173:1), [NoteEditScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/notes/NoteEditScreen.kt:44:0-164:1), [PersonLabelsScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/labels/PersonLabelsScreen.kt:44:0-120:1), [NoteLabelsScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/labels/NoteLabelsScreen.kt:46:0-123:1), [ExportImportScreen](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/export/ExportImportScreen.kt:47:0-159:1).
	Answer: Yes.
  - Decide icon style:
    - e.g. ☀/🌙, tree/moon, or a custom vector.
	Answer: Yes but with proper imports with no errors.
---


# C. Current phase & steps: 


## Phase 1 – Theme Foundation (Add Forest Colors, No Wiring Yet)

- **[Step 1.1] Add forest colors**
  - In [Color.kt](cci:7://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Color.kt:0:0-0:0), define color constants (names like `ForestPrimaryLight`, `ForestPrimaryDark`, etc.) for:
    - Day: greens + warm browns, light backgrounds.
    - Night: deep greens, dark surfaces, high contrast text.
  - Use hex values decided in Phase 0.

- **[Step 1.2] Define forest color schemes**
  - In [Theme.kt](cci:7://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:0:0-0:0), create:
    - `ForestLightColorScheme = lightColorScheme(...)`
    - `ForestDarkColorScheme = darkColorScheme(...)`
  - Map your new color constants into `primary`, `secondary`, `background`, `surface`, `surfaceVariant`, etc.

- **[Step 1.3] Keep current behavior**
  - Do **not** change how [Pep_NotesTheme](cci:1://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/ui/theme/Theme.kt:35:0-57:1) currently chooses `colorScheme`.
  - App should still look exactly the same (purple/dynamic) after this phase.

- **Exit criteria**
  - Project builds.
  - No visual changes yet.
  - Forest color schemes exist and are ready to be used.

---