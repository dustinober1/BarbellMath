# Room Database: Recent Loading Plans History

A local-first Room database integration for Plate Master that automatically records and preserves the user's last 10 calculated loading plans, featuring a dedicated history bottom sheet accessible directly from the top navigation bar with one-tap plan reloading.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and govern the implementation:
> - **Auto-Save Mechanism**: Automatically record calculated plans into Room storage whenever a calculation settles (with an intelligent debounce and duplicate-check to avoid flooding history during stepper clicks).
> - **History Access UI**: A bottom sheet modal opened from a history action button (`Icons.Default.History`) in the top app bar.
> - **One-Tap Reload**: Tapping any saved workout plan immediately populates the calculator with its target weight, barbell type, collars, and plate configuration, closing the sheet smoothly.

- **Confirmed Decision 1**: Store exactly the most recent 10 plans using automated pruning in the Room DAO (`LIMIT 10`).
- **Confirmed Decision 2**: Modern Material 3 bottom sheet styled to match the Violet & Lavender Sleek Interface aesthetic with visual plate color dots and relative timestamps.

---

## 1. Overview & Core Concept

When lifting in the gym, lifters frequently switch between core lifts (e.g. Squat 315 lbs, Bench 225 lbs, Overhead Press 135 lbs) or repeat recent working weights across sessions. Currently, switching weights requires manually re-entering or incrementing the target weight every time.

This feature introduces a reliable, local SQLite database powered by **Android Jetpack Room** that:
1. Persists up to 10 unique recent barbell calculations across app launches.
2. Displays detailed breakdown metadata (barbell used, target weight, plate breakdown per side, and exactness status).
3. Restores any previous setup in a single tap without requiring manual recalibration.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────────────┐
│     Top App Bar         │ ──[Tap History Icon]──► ┌───────────────────────────────────────┐
│  Plate Master   [🕒] [🏋️]│                         │      Recent Loading Plans (10 max)    │
└─────────────────────────┘                         ├───────────────────────────────────────┤
                                                    │ 🏋️ 225 lbs • Olympic Bar (45 lbs)     │
                                                    │    Plates: [45×2] per side   [Exact]  │
                                                    │    Just now               [🗑️ Delete] │
                                                    ├───────────────────────────────────────┤
                                                    │ 🏋️ 185 lbs • Olympic Bar (45 lbs)     │
                                                    │    Plates: [45×1] [25×1]     [Exact]  │
                                                    │    Yesterday at 4:15 PM   [🗑️ Delete] │
                                                    ├───────────────────────────────────────┤
                                                    │ [Clear History]                       │
                                                    └───────────────────────────────────────┘
                                                                       │
                                                         [Tap Card to Quick-Load]
                                                                       ▼
                                                    ┌───────────────────────────────────────┐
                                                    │ Target Weight = 225 lbs               │
                                                    │ Barbell = Olympic (45 lbs)            │
                                                    │ Barbell Canvas & Breakdown Updated    │
                                                    └───────────────────────────────────────┘
```

### Visual Identity & Theme Alignment
- **Bottom Sheet (`LoadingHistorySheet.kt`)**: Smooth `28.dp` top corners, elevated container in `#FEF7FF` / `#F3EDF7`, drag handle, and title pairing ("Recent Plans", "Last 10 workouts").
- **History Cards**: Elevated cards (`#FFFFFF` surface, `16.dp` rounded corners, `1.dp` border `#EADDFF`) showcasing:
  - Big prominent weight text (e.g., `225 lbs`) in Primary Violet (`#6750A4`).
  - Barbell specification subtitle (e.g., `Olympic Bar (Men's) • 45 lbs`).
  - Plate pill sequence with miniature colored dots corresponding to official plate colors (Red 45, Blue 35, Yellow 25, Green 10, White 5, Black 2.5).
  - Exact match badge pill in `PlateGreen` or remainder indicator.
  - Delete icon button (`Icons.Default.DeleteOutline`) with touch target >= 48dp.
- **Empty State**: Friendly gym illustration/icon with helpful copy ("No workout history yet. Calculate a lift to save your first loading plan!").

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Automatic Debounced Saving vs. Immediate Trigger**
  - *Chosen Approach*: When target weight or bar settings change, debounce the save trigger by 800ms and check whether the result is identical to the most recent saved record.
  - *Why*: Prevents creating 15 intermediate records when a user clicks the `+5` or `+25` stepper multiple times in succession.
  - *Alternatives Considered*: Manual save button (rejected by user preference for seamless auto-tracking).

- **Decision 2: 10-Item Hard Limit Enforced at the Database Layer**
  - *Chosen Approach*: After inserting a new record, the DAO executes an automated trim query: `DELETE FROM loading_plans WHERE id NOT IN (SELECT id FROM loading_plans ORDER BY timestamp DESC LIMIT 10)`.
  - *Why*: Guarantees the database never grows unbounded and stays strictly within the 10-item requirement even if accessed asynchronously.

- **Decision 3: Serialization of Plate Breakdown**
  - *Chosen Approach*: Store a concise JSON / comma-delimited representation of the plate counts per side alongside numeric fields for immediate querying and light footprint.
  - *Why*: Avoids secondary relational join tables for simple 10-record history while preserving full fidelity of plate weights and colors.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   BarbellAppScreen                     │
│   TopBar [History Action] ──► LoadingHistorySheet      │
└──────────────────────────┬─────────────────────────────┘
                           │ Observes StateFlow / Dispatches actions
                           ▼
┌────────────────────────────────────────────────────────┐
│                   BarbellViewModel                     │
│  - recentPlans: StateFlow<List<LoadingPlanEntity>>     │
│  - autoSavePlan(result, bar, unit)                     │
│  - loadPlan(plan)                                      │
│  - deletePlan(id) / clearAll()                         │
└──────────────────────────┬─────────────────────────────┘
                           │ Repository Pattern
                           ▼
┌────────────────────────────────────────────────────────┐
│                LoadingPlanRepository                   │
└──────────────────────────┬─────────────────────────────┘
                           │ Flow / Coroutines
                           ▼
┌────────────────────────────────────────────────────────┐
│                     Room Database                      │
│  ┌───────────────────────┐  ┌────────────────────────┐ │
│  │   LoadingPlanDao      │  │   LoadingPlanEntity    │ │
│  └───────────────────────┘  └────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

### Data Model (`LoadingPlanEntity`)
- `id: Long` (Primary key, auto-generated)
- `timestamp: Long` (Epoch milliseconds)
- `targetWeight: Double`
- `achievedWeight: Double`
- `unit: String` ("LBS" or "KG")
- `barId: String` (e.g. "olympic_mens")
- `barName: String`
- `barWeight: Double`
- `customBarWeight: Double`
- `collarsEnabled: Boolean`
- `platesSummary: String` (e.g. `45:2,25:1,10:1`)
- `totalPlatesOnBar: Int`
- `isExact: Boolean`

### Files to Create / Update:
1. `com.example.data.entity.LoadingPlanEntity.kt`: Room entity definition.
2. `com.example.data.dao.LoadingPlanDao.kt`: Room data access object with reactive Flow queries, insert, trim, and delete.
3. `com.example.data.AppDatabase.kt`: RoomDatabase abstract class with singleton accessor.
4. `com.example.data.repository.LoadingPlanRepository.kt`: Repository wrapping Room DAO.
5. `com.example.ui.components.LoadingHistorySheet.kt`: Material 3 bottom sheet with styled plan cards and quick-reload actions.
6. `com.example.ui.BarbellViewModel.kt`: Inject repository, observe recent plans, debounced auto-save, and `loadPlan()` action.
7. `com.example.ui.BarbellAppScreen.kt`: Add history button to TopAppBar and integrate `LoadingHistorySheet`.
8. `ExampleRobolectricTest.kt`: Unit test verifying Room insertion, 10-item cap pruning, and quick-reload logic.
