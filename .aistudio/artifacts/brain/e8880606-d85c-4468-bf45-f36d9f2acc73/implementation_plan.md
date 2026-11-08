# Room Database: Recent Loading Plans History

A robust, device-local SQLite database powered by Android Jetpack Room that automatically persists and manages the user's last 10 calculated barbell loading plans, complete with a responsive Material 3 history bottom sheet and one-tap calculation restoration.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and govern the implementation:
> - **Auto-Save Timing**: The app will automatically save loading plans after an 800ms debounce pause when adjusting barbell target weights or equipment, avoiding redundant entries during rapid stepper adjustments.
> - **History Access**: Users access recent loading history via a dedicated history icon button (`Icons.Default.History`) in the top app bar, opening an ergonomic Material 3 bottom sheet modal with badge counters.
> - **One-Tap Load**: Tapping any saved workout entry instantly populates the calculator with its target weight, bar type, collars configuration, and plate inventory, immediately switching back to the active calculator screen.

- **Confirmed Decision 1: 10-Item Hard Limit**: Enforced directly at the database layer via automated pruning SQL query so the database never exceeds 10 records.
- **Confirmed Decision 2: Deduplication Check**: If the newly calculated setup has the identical target weight, barbell type, and plate breakdown as the most recent record, duplicate insertion is suppressed.
- **Confirmed Decision 3: Individual & Bulk Deletion**: Users can delete individual history records with a swipe/tap or clear all 10 saved plans with a confirmation modal.

---

## 1. Overview & Core Concept

- **What It Does**: Automatically tracks the user's recent barbell loading calculations without requiring manual bookmarking. It stores up to 10 unique workout setups, showing barbell specifications, target weights, plate breakdown per side, and whether an exact weight match was achieved.
- **Target Audience / Persona**: Strength athletes, powerlifters, and gym-goers who alternate between recurring working sets (e.g. 135 lb warm-up, 225 lb bench press, 315 lb squat) and need quick, frictionless switching between recent configurations.
- **Key Value**: Eliminates repetitive keypad entries and barbell reconfiguration in gym environments by preserving the last 10 loading solutions offline, surviving app restarts and process recreation.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────────────────────┐
│       Top Navigation Bar        │
│  Plate Master      [🕒 3] [🏋️]   │
└────────────────┬────────────────┘
                 │ Tap History Action Icon
                 ▼
┌─────────────────────────────────────────────────────────┐
│ Modal Bottom Sheet: "Recent Plans"                      │
│ 3 of 10 workouts saved                    [Clear All]   │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 225 lbs  [Exact]                          Just now  │ │
│ │ Olympic Bar (Men's) • 45 lbs                        │ │
│ │ [🔴 45 × 2]                                  [Load] │ │
│ └─────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 185 lbs  [Exact]                            2h ago  │ │
│ │ Olympic Bar (Men's) • 45 lbs                        │ │
│ │ [🔴 45 × 1] [🟡 25 × 1]                     [Load] │ │
│ └─────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────┘
                             │ Tap Record Card
                             ▼
┌─────────────────────────────────────────────────────────┐
│ Calculator Screen Restored                              │
│ • Target Weight: 225 lbs                                │
│ • Barbell: Olympic Bar (Men's)                          │
│ • Interactive Canvas & Breakdown Reloaded               │
└─────────────────────────────────────────────────────────┘
```

### Visual Identity & Theme
- **Color Palette & Accents**: Primary Violet (`#6750A4`) for weight highlights, `PlateGreen` (`#388E3C`) for exact-match badge chips, dynamic surface containers (`#FEF7FF` / `#F3EDF7`), and subtle outline borders (`#EADDFF`).
- **Plate Breakdown Chips**: Each history card visually displays the plate pairs loaded per side, featuring colored circular indicators matching real bumper plates (Red 45s, Blue 35s, Yellow 25s, Green 10s, White 5s, Black 2.5s).
- **Typography**: Bold display weights (`titleMedium`, `FontWeight.Black`) for immediate legibility at arm's length in high-glare gym environments, paired with clean relative time spans ("Just now", "15m ago", "Yesterday").
- **Interactive Feedback**: Material ripple indications on card taps, tactile icon buttons with minimum 48dp touch targets, animated badges on the top bar history icon reflecting active plan count, and clear empty state graphics when no workouts are stored yet.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Debounced Auto-Save vs. Manual Save Button**
  - *Chosen Approach*: Automatic debounced background save (800ms delay) whenever calculations settle.
  - *Why*: In heavy training sessions, lifters rarely want to remember to press "Save" after every calculation. An automated debounce ensures smooth stepper adjustment (+5, +10 lbs) without flooding history with intermediate states.
  - *Alternatives Considered*: Manual bookmarking button (adds friction and user cognitive load).

- **Decision 2: Enforcing Maximum 10 Records at the DAO Level**
  - *Chosen Approach*: Directly inside the DAO, a pruning query runs immediately after insertion: `DELETE FROM loading_plans WHERE id NOT IN (SELECT id FROM loading_plans ORDER BY timestamp DESC LIMIT :keepCount)`.
  - *Why*: Keeps database queries bounded, eliminates unbounded growth, and enforces the prompt's 10-plan constraint deterministically.

- **Decision 3: Serialization Strategy for Plate Breakdown**
  - *Chosen Approach*: Delimited token string (`"weight:quantity,weight:quantity"`) stored directly in the entity, paired with Kotlin helper extension functions to parse into typed plate pairs.
  - *Why*: Fast, zero-dependency serialization for SQLite without requiring heavyweight multi-table joins or relational schemas for temporary historical snapshots.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI                       │
│   BarbellAppScreen ◄──────► LoadingHistorySheet (Bottom Sheet)  │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                   Observes recentPlans StateFlow
                   Invokes loadPlan(), deletePlan(), clearAll()
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BarbellViewModel                         │
│  - recentPlans: StateFlow<List<LoadingPlanEntity>>              │
│  - autoSaveJob: Debounced Coroutine Job (800ms)                 │
│  - loadPlan(entity): Restores targetWeight, bar, and collars    │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                     Repository Abstraction Layer
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     LoadingPlanRepository                       │
│  - recentPlans: Flow<List<LoadingPlanEntity>> (LIMIT 10)        │
│  - savePlan(entity): Deduplication check + insert + prune       │
│  - deletePlanById(id): Removes single item                      │
│  - clearHistory(): Wipes table                                  │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                       Room SQLite Database
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                         AppDatabase                             │
│  ┌───────────────────────────┐   ┌───────────────────────────┐  │
│  │      LoadingPlanDao       │   │     LoadingPlanEntity     │  │
│  │  - getRecentPlans(10)     │   │  - id: Long (PK)          │  │
│  │  - insertPlan(plan)       │   │  - targetWeight: Double   │  │
│  │  - trimOldPlans(10)       │   │  - platesSummary: String  │  │
│  │  - deletePlanById(id)     │   │  - barId, barName, unit   │  │
│  │  - clearAllPlans()        │   │  - timestamp: Long        │  │
│  └───────────────────────────┘   └───────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Data Model & Entity Schema
- `id: Long`: Primary Key with auto-increment.
- `timestamp: Long`: Unix millisecond timestamp for relative time display and ordering.
- `targetWeight: Double`: Goal weight requested by user.
- `achievedWeight: Double`: Actual total weight loaded on bar based on available plate inventory.
- `unit: String`: "LBS" or "KG".
- `barId: String` & `barName: String`: Selected barbell identifier and descriptive name.
- `barWeight: Double` & `customBarWeight: Double`: Barbell tare weight.
- `collarsEnabled: Boolean`: Collar weight status.
- `platesSummary: String`: Encoded plate distribution per side (e.g., `"45.0:2,25.0:1"`).
- `totalPlatesOnBar: Int`: Total count of plates across both sleeves.
- `isExact: Boolean`: Flag indicating whether the achieved weight matched the target exactly.

### Interactive State Transitions
1. **Target Adjustment**: As user types a weight or clicks steppers, `BarbellCalculator.calculatePlates` recalculates results immediately.
2. **Auto-Save Settling**: After 800ms of user inactivity, `scheduleAutoSave()` verifies the record isn't identical to the most recent entry, then inserts it and trims history to 10 entries.
3. **History Inspection**: User taps the history icon in the top app bar; `LoadingHistorySheet` displays a scrollable list of recent plans with relative time, plate chips, and delete options.
4. **Workout Restoration**: Tapping a plan card triggers `viewModel.loadPlan(plan)`, updating target weight, bar type, and collars, resetting active tab to Calculator, and dismissing the bottom sheet.
