# Phases & Steps – Themes, Day/Night Toggle, Menu Icons

Context (current code):
- Theme: `Pep_NotesTheme(darkTheme: Boolean)` in `ui/theme/Theme.kt` with Forest-only color schemes.
- Persistence: `ThemeViewModel` with `isForestDark: StateFlow<Boolean>` using `DataStore`.
- UI:
  - Day/Night toggle: `IconButton` in `PeopleListScreen` using `Icons.Default.Star` and `onToggleTheme`.
  - Menu: `MenuDialog` + `MenuItem` in `PeopleListScreen.kt` with text-only options:
    - Manage Labels  
    - Backup & Restore  
    - About Us  
    - Exit  

Below is a **self‑contained, incremental plan**. Each phase compiles and works on its own; later phases build on earlier ones.

---

## Phase 1 – Theme model & persistence (no new UI yet)

**Goal:** Introduce `AppTheme` and more generic persistence while keeping behavior identical (Forest + Day/Night).

- **Step 1.1 – Define `AppTheme`**
  - Create e.g. `ui/theme/AppTheme.kt`.
  - Add:
    - `enum class AppTheme { FOREST, OCEAN, DESERT, GOTHAM }`
  - For now, app will still *use only* `FOREST`, but model is ready.

- **Step 1.2 – Extend `ThemeViewModel` state**
  - In `ThemeViewModel.kt`:
    - Replace `_isForestDark` with something like:
      - Either two flows: `currentTheme: StateFlow<AppTheme>`, `isDark: StateFlow<Boolean>`
      - Or a single `StateFlow<ThemeState>` (`data class ThemeState(val appTheme: AppTheme, val isDark: Boolean)`).
    - Keep default as: `AppTheme.FOREST` + `isDark = false` (Day).

- **Step 1.3 – Update DataStore keys**
  - Keep existing boolean key for compatibility:
    - `IS_FOREST_DARK` → treat it as **generic** `isDark`.
  - Add a new string (or int) key for theme:
    - e.g. `APP_THEME = stringPreferencesKey("app_theme")`.
  - On init:
    - Read `APP_THEME`; if null → use `FOREST`.
    - Read `IS_FOREST_DARK`; if null → `false` (Day).
  - Expose read-only flows to UI.

- **Step 1.4 – Update `toggleTheme()`**
  - In `ThemeViewModel`:
    - Flip only `isDark` for the **current** theme.
    - Persist new `isDark` value to DataStore.

**Result after Phase 1:**
- App still visually behaves the same (Forest + Day/Night).
- Internally, theme is modeled as `AppTheme` + `isDark` and persisted.

---

## Phase 2 – Color palettes & `Pep_NotesTheme` support for all themes

**Goal:** Add Ocean, Desert, Gotham palettes (light & dark) and make `Pep_NotesTheme` choose scheme based on `AppTheme` + `isDark`.

- **Step 2.1 – Define new colors in `Color.kt`**
  - For **each** theme (Ocean, Desert, Gotham) define:
    - `PrimaryLight`, `OnPrimaryLight`, `SecondaryLight`, `OnSecondaryLight`, `BackgroundLight`, `OnBackgroundLight`, `SurfaceLight`, `OnSurfaceLight`, `SurfaceVariantLight`, `OnSurfaceVariantLight`.
    - Same set for `Dark`.
  - Pick palettes that match:
    - **Ocean**: blues/teals (Azure), calm surfaces.
    - **Desert**: warm sand/sunset oranges, brown accents.
    - **Gotham**: high-contrast gray/black with off-white text.

- **Step 2.2 – Add color schemes in `Theme.kt`**
  - Next to `ForestDarkColorScheme` / `ForestLightColorScheme`, define:
    - `OceanDarkColorScheme`, `OceanLightColorScheme`
    - `DesertDarkColorScheme`, `DesertLightColorScheme`
    - `GothamDarkColorScheme`, `GothamLightColorScheme`

- **Step 2.3 – Update `Pep_NotesTheme` signature**
  - Change from:
    - `fun Pep_NotesTheme(darkTheme: Boolean, content: ...)`
  - To:
    - `fun Pep_NotesTheme(appTheme: AppTheme, darkTheme: Boolean, content: ...)`
  - Inside, choose `colorScheme` via `when (appTheme)` and `darkTheme`.

- **Step 2.4 – Wire [MainActivity](cci:2://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:20:0-45:1) to new API**
  - In [MainActivity](cci:2://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/src/main/java/com/horizone/pep_notes/MainActivity.kt:20:0-45:1):
    - Collect both `currentTheme` and `isDark` from `ThemeViewModel`.
    - Replace:
      - `Pep_NotesTheme(darkTheme = dark) { ... }`
    - With:
      - `Pep_NotesTheme(appTheme = currentTheme, darkTheme = isDark) { ... }`
  - Keep Day/Night `Crossfade` keyed by full theme state if desired.

**Result after Phase 2:**
- All 4 themes have color definitions.
- App still only **uses Forest**, but architecture supports switching themes without breaking anything.

---

## Phase 3 – Day/Night toggle behavior & icons on main screen

**Goal:** Use sun/moon icons and tie them to actual `isDark` state for the *current* theme. Toggle remains only on `PeopleListScreen`.

- **Step 3.1 – Ensure icons dependency**
  - In [app/build.gradle.kts](cci:7://file:///c:/Users/GNeSH/Desktop/Code/Pep_Notes/P_Notes/app/build.gradle.kts:0:0-0:0) `dependencies { ... }` add (if not already present and compiling):
    - `implementation("androidx.compose.material:material-icons-extended")`
  - This is needed for icons like `Brightness7`, `Brightness4`, `Palette`, `Label`, etc.

- **Step 3.2 – Propagate `isDark` to `PeopleListScreen`**
  - In `AppNavHost` (nav layer):
    - Add a Boolean parameter, e.g. `isDark: Boolean` (or `themeState`).
    - Pass it down to `PeopleListScreen`.
  - Update `PeopleListScreen` signature to accept `isDark: Boolean`.

- **Step 3.3 – Replace star with sun/moon**
  - In `PeopleListScreen`, top row IconButton currently uses:
    - `Icon(imageVector = Icons.Default.Star, ...)`
  - Change logic to:
    - If `isDark == false` (Day) → show **Sun**:
      - `Icons.Filled.Brightness7`
    - If `isDark == true` (Night) → show **Moon**:
      - `Icons.Filled.Brightness4` (or `Icons.Filled.DarkMode`)
  - Ensure imports (no errors):
    - `import androidx.compose.material.icons.Icons`
    - `import androidx.compose.material.icons.filled.Brightness7`
    - `import androidx.compose.material.icons.filled.Brightness4` (or `DarkMode`)

- **Step 3.4 – Keep behavior same across themes**
  - `onToggleTheme()` only flips `isDark` for the **current `AppTheme`**.
  - The icon just reflects `isDark`, not the specific theme.

**Result after Phase 3:**
- On main screen:
  - Day: sun icon shown, tap → Night.
  - Night: moon icon shown, tap → Day.
- Works for all (current and future) themes by reusing same toggle.

---

## Phase 4 – Menu icons & Theme menu entry

**Goal:** Enhance the menu dialog with icons and add a “Theme” / “Theme chooser” entry that navigates to a separate preview screen.

- **Step 4.1 – Extend `MenuItem` composable for icons**
  - In `PeopleListScreen.kt` `MenuItem`:
    - Add a parameter: `icon: ImageVector`.
    - In the UI row, show that icon at the start / end next to label, tinted with `MaterialTheme.colorScheme.primary`.
  - Update imports:
    - `import androidx.compose.material.icons.Icons`
    - `import androidx.compose.material.icons.filled.*` (or specific icons).

- **Step 4.2 – Assign icons to existing options**
  - In `MenuDialog`:
    - **Manage Labels**:
      - Icon: `Icons.Filled.Label`
    - **Backup & Restore**:
      - Icon: `Icons.Filled.Backup` (or `Icons.Filled.CloudUpload` / `CloudDownload`, pick what feels best)
    - **About Us**:
      - Icon: `Icons.Filled.Info`
    - **Exit**:
      - Icon: `Icons.Filled.ExitToApp`
  - Verify:
    - All icon names exist in `material-icons-extended` for compose.
    - Imports are explicit for any icon *not* used elsewhere.

- **Step 4.3 – Add Theme chooser item in menu**
  - In `MenuDialog` text content column, between “Backup & Restore” and “About Us” (or where you prefer), add:
    - `MenuItem(label = "Theme", icon = Icons.Filled.Palette, onClick = { ... })`
  - On click:
    - Close `MenuDialog` (call `onDismiss()`).
    - Navigate to a new route, e.g. `NavRoutes.ThemePicker.route`.

- **Step 4.4 – Add nav route for Theme Picker**
  - In `NavRoutes` (where routes are defined):
    - Add e.g. `object ThemePicker : NavRoutes("theme_picker")`.
  - In `AppNavHost`:
    - Add `composable(NavRoutes.ThemePicker.route) { ThemePickerScreen(...) }`.

**Result after Phase 4:**
- Menu dialog now has clear icons and a **Theme** entry accessed from the triple-dot button.
- Navigation to theme chooser screen is wired, but the screen itself can be minimal at this point.

---

## Phase 5 – Theme Picker screen with previews & apply actions

**Goal:** Provide a dedicated screen showing previews of Forest / Ocean / Desert / Gotham (each with its own light/dark look), and allow user to select/apply theme. Uses the same Day/Night toggle on main screen.

- **Step 5.1 – Create `ThemePickerScreen` composable**
  - New file, e.g. `ui/theme/ThemePickerScreen.kt`.
  - Receive from caller:
    - Current `AppTheme`
    - `isDark` (optional; mainly for highlighting which variant is active).
    - Callbacks: `onThemeSelected(AppTheme)` & maybe `onBack()`.

- **Step 5.2 – Layout**
  - Show a list/grid of 4 cards:
    - Forest, Ocean, Desert, Gotham.
  - Each card:
    - Uses its palette to render a mini preview:
      - Small top bar, background, primary button sample, text.
    - Shows:
      - Theme name.
      - Tag/label like “Current” if it matches `currentTheme`.
    - “Apply” button that calls `onThemeSelected(appTheme)`.

- **Step 5.3 – Wire to `ThemeViewModel`**
  - In `AppNavHost` when composing `ThemePickerScreen`:
    - Obtain `ThemeViewModel` (via `hiltViewModel()`).
    - Pass current theme + isDark from viewmodel.
    - Implement `onThemeSelected(appTheme)`:
      - Call `themeViewModel.setTheme(appTheme)` (new function).
      - Optional: navigate `popBackStack()` to return to PeopleListScreen.
  - `setTheme(appTheme)` in `ThemeViewModel`:
    - Update current AppTheme in state + DataStore.
    - Preserve `isDark` (don’t auto-flip).

- **Step 5.4 – Consistency with Day/Night toggle**
  - User flow:
    - Choose theme (Forest/Ocean/Desert/Gotham) via Theme Picker.
    - Return to main screen; **same Day/Night toggle** now flips light/dark variant of the selected theme.
  - No separate theme-level day/night; you rely on the one global toggle you already have.

**Result after Phase 5:**
- User can:
  - Open menu → Theme → see previews of 4 themes.
  - Apply any theme, which persists.
  - Use top-row Day/Night toggle to flip between that theme’s light/dark variant.
- All icons and imports are explicit, and the behavior of earlier phases remains intact.

---

## Phase 6 – Polish & safeguards

**Goal:** Final cleanup so code is robust and consistent.

- **Step 6.1 – Defensive defaults**
  - In `ThemeViewModel`:
    - If `APP_THEME` DataStore value is unknown → fallback to `FOREST`.
- **Step 6.2 – Remove old naming**
  - Optionally rename `IS_FOREST_DARK` constant to something generic (e.g. `IS_DARK_THEME`) while preserving underlying key string for migration safety.
- **Step 6.3 – Visual tuning**
  - Adjust each theme’s color palette for readability:
    - Ensure sufficient contrast in Desert and Gotham, especially for text on backgrounds.
  - Confirm icons tinting looks consistent with `MaterialTheme.colorScheme.primary` and surfaces.

---