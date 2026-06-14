---

# KHATA
## Real-Time Decentralized Financial Ledger & Mess Tracker

---

| | |
|---|---|
| **Project Title** | Khata — Real-Time Financial Ledger & Mess Tracker |
| **Platform** | Android (Native) |
| **Language** | Kotlin |
| **Submitted By** | [Your Full Name] |
| **Roll Number** | [Your Roll Number] |
| **Department** | [Your Department] |
| **Institution** | [Your University Name] |
| **Supervisor** | [Supervisor's Name] |
| **Submission Date** | June 2026 |
| **GitHub Repository** | [Insert Link Here] |
| **APK File** | Attached / [Insert Download Link] |

---

## Table of Contents

1. [Abstract](#1-abstract)
2. [Introduction](#2-introduction)
3. [Problem Statement](#3-problem-statement)
4. [Objectives](#4-objectives)
5. [Scope of the Project](#5-scope-of-the-project)
6. [Literature Review / Background Study](#6-literature-review--background-study)
7. [System Architecture](#7-system-architecture)
8. [Technology Stack](#8-technology-stack)
9. [Database Design](#9-database-design)
10. [Module Descriptions](#10-module-descriptions)
11. [Key Algorithms](#11-key-algorithms)
12. [Implementation Details](#12-implementation-details)
13. [Application Screenshots](#13-application-screenshots)
14. [Testing & Validation](#14-testing--validation)
15. [Challenges Faced & Solutions](#15-challenges-faced--solutions)
16. [Future Enhancements](#16-future-enhancements)
17. [Conclusion](#17-conclusion)
18. [References](#18-references)

---

## 1. Abstract

**Khata** is a native Android mobile application designed to solve the real-world financial management problems faced by students living in hostels or shared accommodations. Managing shared expenses among roommates — tracking who paid for what, who owes whom, and calculating fair shares of monthly mess (food) bills — is a common source of confusion and disputes in communal living environments.

This project delivers a fully offline, cryptographically secure, and mathematically precise solution. Khata implements a reactive data architecture using Kotlin Coroutines and Flow to provide live, real-time balance updates. The core financial logic is powered by high-precision `BigDecimal` arithmetic to eliminate floating-point errors, while an O(N log N) Greedy Debt Simplification Algorithm minimizes the total number of financial transactions required to settle debts within a group.

The application is built on Clean Architecture principles using Jetpack Compose for the UI, Room Database for data persistence, Hilt for dependency injection, and SQLCipher for AES-256 at-rest database encryption.

---

## 2. Introduction

Students living in hostels frequently encounter the following recurring problems:
- One person buys groceries and needs to collect money from others.
- Monthly mess costs need to be divided fairly based on how many meals each person ate.
- Keeping a manual record in a notebook or chat group causes confusion and is error-prone.
- Money disputes arise because there is no reliable, shared, trusted source of truth.

**Khata** (the Urdu/Hindi word for *ledger*) directly addresses this gap. It provides a structured, automated, and mathematically reliable tool for tracking shared expenses, logging daily meals, and calculating exactly who owes what to whom at any given moment.

Unlike cloud-based alternatives such as Splitwise, Khata is 100% offline-first. All data is stored locally on the device, encrypted with AES-256, and is never transmitted to any external server. This design is particularly suitable for users with limited data connectivity and a strong need for financial privacy.

---

## 3. Problem Statement

Communal financial management in hostel settings suffers from three core problems:

**Problem 1 — Expense Tracking Inaccuracy**
Manual tracking in notebooks or messaging applications is unstructured and error-prone. Payments are forgotten, overwritten, or incorrectly recorded. There is no automatic calculation of balances.

**Problem 2 — Unfair Mess Bill Distribution**
Monthly grocery costs are often split equally, even though members consume different numbers of meals. A student who was absent for a week should not pay the same amount as someone who ate every meal.

**Problem 3 — Complex Debt Resolution**
In a group of N people, understanding the precise financial obligations (who pays whom, and how much) becomes computationally complex. Naive approaches result in a large number of redundant transactions.

**Khata solves all three problems** through structured data models, a weighted meal-cost algorithm, and a greedy debt simplification engine.

---

## 4. Objectives

The primary objectives of this project are:

1. **Provide a reliable expense tracking system** that allows group members to log shared expenses with flexible split types (equal, exact, percentage) and automatically update all members' balances in real-time.

2. **Implement a fair Mess Bill Calculator** that weights each meal type (Breakfast, Lunch, Dinner) and distributes the monthly grocery cost proportionally based on individual meal consumption.

3. **Design a Debt Simplification Engine** to compute the minimum set of financial transactions required to settle all debts within a group, reducing friction and confusion.

4. **Ensure data security** through local AES-256 encryption of all financial data, with no cloud dependency.

5. **Deliver a production-quality Android application** following Clean Architecture principles, with a reactive, live UI that reflects real-time financial state.

---

## 5. Scope of the Project

### In Scope
- Group creation and member management (name, phone, room number)
- Shared expense logging with Equal, Exact, and Percentage split modes
- Settlement tracking (recording when one member pays another)
- Real-time reactive balance calculation (You Paid / You Owe / Net Balance)
- Monthly Mess (meal) tracking with a calendar-based daily log interface
- Automated mess bill calculation with configurable meal weights
- Debt simplification showing the minimum transactions to settle all debts
- AES-256 local database encryption via SQLCipher
- Multi-currency support (PKR, USD, EUR, GBP, SAR, AED)
- User profile management (name, contact, room)

### Out of Scope
- Cloud synchronization or multi-device real-time sync
- Bank or EasyPaisa/JazzCash API-based automatic payments
- OCR receipt scanning
- Multi-group cross-settlement

---

## 6. Literature Review / Background Study

### 6.1 Existing Solutions

| Application | Type | Limitation |
|---|---|---|
| **Splitwise** | Cloud-based | Requires internet; free tier has limits; no Urdu/PKR focus |
| **Settle Up** | Cloud-based | No meal tracking; online dependency |
| **Excel / Notebooks** | Manual | Error-prone; no automation; no version control |
| **WhatsApp Recording** | Informal | Completely unstructured; no calculations |

None of the above solutions address the specific problem of **proportional mess bill splitting combined with real-time offline expense tracking** in a single, encrypted application.

### 6.2 Relevant Technologies

**Jetpack Compose** (Google, 2021): A modern, declarative UI toolkit for Android that simplifies UI development by describing what the UI should look like for a given state, rather than imperatively manipulating views.

**Room Database** (Google): An abstraction layer over SQLite that provides compile-time verification of SQL queries and integration with Kotlin Coroutines and Flow for reactive programming.

**Kotlin Coroutines & Flow** (JetBrains): A concurrency framework enabling asynchronous, non-blocking data streams. `Flow<T>` provides a reactive stream that automatically notifies collectors when underlying data changes.

**SQLCipher** (Zetetic): An open-source extension to SQLite that provides transparent 256-bit AES encryption of database files.

**Greedy Debt Simplification** (Classic Algorithm): A well-established approach to the "minimum cash flow" problem using two priority queues (max-heap for creditors, min-heap for debtors) to greedily match and settle the largest outstanding debts first, achieving optimal results in O(N log N) time.

---

## 7. System Architecture

Khata follows the **Clean Architecture** pattern as defined by Robert C. Martin (Uncle Bob), adapted for Android with the MVVM (Model-View-ViewModel) presentation pattern.

```
┌─────────────────────────────────────────────────────────────┐
│               PRESENTATION LAYER                             │
│        (Jetpack Compose UI + ViewModels)                    │
│  DashboardScreen | ExpenseScreen | MealScreen | Settings    │
└──────────────────────────┬──────────────────────────────────┘
                           │  observes StateFlow / events
┌──────────────────────────▼──────────────────────────────────┐
│                  DOMAIN LAYER                                │
│           (Pure Kotlin — No Android Dependencies)           │
│  Use Cases | Repository Interfaces | Domain Models          │
│  DebtSimplifier | CalculateMessBillUseCase                  │
└──────────────────────────┬──────────────────────────────────┘
                           │  implements interfaces
┌──────────────────────────▼──────────────────────────────────┐
│                   DATA LAYER                                 │
│       (Android / Framework — Can be Swapped)                │
│  Room DAOs | SQLCipher DB | Repository Implementations      │
│  ExpenseDao | SettlementDao | MealDao | UserDao             │
└─────────────────────────────────────────────────────────────┘
```

### 7.1 Design Principles Applied

| Principle | Application in Khata |
|---|---|
| **Separation of Concerns** | UI has zero business logic; all logic resides in UseCases |
| **Dependency Inversion** | Domain layer depends only on interfaces, not implementations |
| **Single Responsibility** | Each UseCase performs exactly one well-defined operation |
| **Reactive Programming** | Room Flows automatically push updates to the UI |
| **Financial Precision** | `BigDecimal` used exclusively for all monetary calculations |

### 7.2 Data Flow Diagram

```
User Action (e.g., Add Expense)
        │
        ▼
  Composable (UI)
        │  calls
        ▼
  ViewModel.addExpense()
        │  calls UseCase
        ▼
  ExpenseUseCases.addExpense()
        │  calls Repository
        ▼
  ExpenseRepositoryImpl
        │  calls DAO
        ▼
  Room Database (SQLCipher encrypted)
        │
        │  Room emits update via Flow
        ▼
  observeNetBalances() Flow triggers
        │
        ▼
  ViewModel.uiState updates
        │
        ▼
  DashboardScreen re-composes automatically
```

---

## 8. Technology Stack

### 8.1 Core Technologies

| Technology | Version | Role |
|---|---|---|
| **Kotlin** | 2.0.0 | Primary programming language |
| **Android SDK** | API 26 – 34 | Target platform (Android 8.0+) |
| **JDK** | 17 | Java toolchain for compilation |
| **Gradle** | 9.4.1 | Build automation |

### 8.2 UI & Navigation

| Library | Version | Role |
|---|---|---|
| **Jetpack Compose BOM** | 2024.09.00 | Declarative UI toolkit |
| **Material Design 3** | via BOM | Component library & theming |
| **Navigation Compose** | 2.8.0 | Type-safe in-app navigation |
| **Vico Charts** | 2.0.0-alpha.20 | Financial analytics visualizations |

### 8.3 Architecture & Data

| Library | Version | Role |
|---|---|---|
| **Hilt (Dagger)** | 2.52 | Compile-time dependency injection |
| **Room Database** | 2.6.1 | SQLite ORM with Flow support |
| **Kotlin Coroutines** | 1.8.1 | Async operations & reactive streams |
| **Kotlin Serialization** | 1.7.1 | JSON serialization for complex data |
| **DataStore Preferences** | 1.1.1 | Key-value storage for settings |

### 8.4 Security

| Library | Version | Role |
|---|---|---|
| **SQLCipher** | 4.5.6 | AES-256 database encryption |
| **AndroidX Security Crypto** | 1.1.0-alpha06 | Encrypted SharedPreferences |
| **AndroidX Biometric** | 1.2.0-alpha05 | Fingerprint/face unlock |

---

## 9. Database Design

### 9.1 Entity Relationship Overview

The database consists of **7 tables** managing Users, Groups, Expenses, Participants, Settlements, Meal Plans, and Meal Logs.

```
users ──────────────────────────────────────────┐
  │ id (PK)                                      │
  │ name                            groups        │
  │ phone                             │ id (PK)  │
  │ roomNumber                        │ name     │
  │ isCurrentUser                     │ currency │
  │                                   │          │
expenses ──────────────────┐           │          │
  │ id (PK)                │           │          │
  │ groupId (FK→groups)    │           │          │
  │ description            │           │          │
  │ amount (TEXT/BigDecimal)│          │          │
  │ payerId (FK→users)     │           │          │
  │ splitType              │           │          │
  │ category               │           │          │
  │ createdAt              │           │          │
  │                        │           │          │
expense_participants ───────┘           │          │
  │ id (PK)                            │          │
  │ expenseId (FK→expenses)            │          │
  │ userId (FK→users) ─────────────────┘          │
  │ owedAmount (TEXT/BigDecimal)                   │
  │                                               │
settlements                                       │
  │ id (PK)                                       │
  │ groupId (FK→groups)                           │
  │ payerUserId (FK→users) ───────────────────────┘
  │ receiverUserId (FK→users)
  │ amount (TEXT/BigDecimal)
  │ settledAt
  │
meal_plans
  │ id (PK)
  │ groupId (FK→groups)
  │ month (Int)
  │ year (Int)
  │ breakfastWeight (BigDecimal)
  │ lunchWeight (BigDecimal)
  │ dinnerWeight (BigDecimal)
  │ totalGroceryCost (BigDecimal)
  │
meal_logs
  │ id (PK)
  │ mealPlanId (FK→meal_plans)
  │ userId (FK→users)
  │ date (Long)
  │ hadBreakfast (Boolean)
  │ hadLunch (Boolean)
  │ hadDinner (Boolean)
```

### 9.2 Key Database Design Decisions

**BigDecimal as TEXT:** All monetary values (`amount`, `owedAmount`, `billAmount`) are stored as `TEXT` in SQLite using a Room `TypeConverter`. This preserves full precision across the decimal representation, preventing the rounding errors inherent to floating-point types (`REAL`/`DOUBLE`).

**Immutable Expense Records:** Expenses are never modified after creation. An expense with an incorrect amount is corrected by deleting and re-adding. This preserves a clean audit trail.

**Settlement as a Separate Entity:** Settlements (debt payments) are maintained as a separate table from expenses. This allows the reactive balance calculation to combine both data streams independently (expense debts minus settlement payments = true net balance).

---

## 10. Module Descriptions

### 10.1 Onboarding Module
**Purpose:** First-run user setup.
- Prompts the user to enter their name, phone number, and room number.
- Creates the User record in the database and marks `isCurrentUser = true`.
- Creates the default group ("My Hostel Room") automatically.

### 10.2 Dashboard Module
**Purpose:** The home screen and central hub.
- Displays the **Balance Card**: Net Balance, You Paid, You Owe — all calculated reactively from live database streams.
- Shows **Recent Expenses** with payer name resolution.
- Provides quick-access navigation buttons: Expenses, Meals, Members.
- Hosts a Floating Action Button for adding new expenses.

**Reactive Balance Logic:**
The Dashboard does not calculate balances itself. It observes a `Flow<Result<List<Balance>>>` from the `ExpenseRepository`. This flow is a 4-way combination of:
1. Total paid per user (from `expenses` table)
2. Total owed per user (from `expense_participants` table)
3. Total paid via settlements (from `settlements.payerUserId`)
4. Total received via settlements (from `settlements.receiverUserId`)

When any of these four streams emit a new value (triggered by a Room database write), the ViewModel automatically receives the updated combined balance and updates the UI.

### 10.3 Expense Module
**Purpose:** Full expense lifecycle management.

**Sub-features:**
- **Expense List:** Displays all expenses for the current group, sorted by creation date. Shows description, amount, and the payer's name.
- **Add Expense:** A comprehensive form allowing:
  - Description and amount entry
  - Currency selection (PKR, USD, EUR, etc.)
  - Category selection (Food, Utilities, Rent, Transport)
  - Split Type selection (Equal / Exact / Percentage)
  - Participant selection and per-participant amount entry

### 10.4 Settlement Module
**Purpose:** Record payments made between members to settle debts.
- The **Settle Up** screen runs the `SimplifyDebtsUseCase` to compute the minimum set of transactions.
- Displays a clear list: "Hamza should pay Asad Rs. 270"
- Tapping "Mark as Settled" inserts a `SettlementEntity`, which immediately triggers the reactive balance flow to recalculate.

### 10.5 Meal Tracker (Mess) Module
**Purpose:** Daily meal logging and proportional bill calculation.

**Sub-features:**
- **Calendar View:** An interactive monthly calendar. Tapping a date expands it to show Breakfast / Lunch / Dinner toggles.
- **Monthly Bill Calculator:** The user enters the total grocery spend for the month. The `CalculateMessBillUseCase` distributes this cost proportionally.
- **Daily Meal Logs:** A scrollable list showing each day's meal entries.

### 10.6 Members Module
**Purpose:** View and manage group membership.
- Displays all group members with their avatar initial, name, phone, and room number.
- Marks the current user with a "(You)" label.
- Provides an "Add Member" button to add new roommates.

### 10.7 Settings Module
**Purpose:** Application configuration and profile management.
- **Edit Profile:** Users can update their name, phone number, and room number. Changes are persisted to the database instantly.

---

## 11. Key Algorithms

### 11.1 Debt Simplification Algorithm

**Class:** `DebtSimplifier.kt`  
**Location:** `domain/algorithm/`  
**Complexity:** O(N log N)

**Problem:** Given N people each with a net balance (positive = owed money, negative = owes money), find the minimum number of transactions to settle all debts.

**Algorithm (Greedy Dual-Heap Approach):**

```
Step 1: Compute each user's net balance
        net[i] = totalPaid[i] - totalOwed[i]

Step 2: Separate into two priority queues:
        creditors = MaxHeap { net[i] > 0.01 }   (owed money)
        debtors   = MinHeap { net[i] < -0.01 }  (owes money)

Step 3: While both heaps are non-empty:
        a. Pop the largest creditor (most owed)
        b. Pop the largest debtor (most owes)
        c. settlementAmount = min(credit, |debit|)
        d. Record transaction: debtor → creditor for settlementAmount
        e. Re-insert remaining balances back into heaps

Step 4: Return list of minimal transactions
```

**Kotlin Implementation Extract:**
```kotlin
while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
    val creditor = creditors.poll()  // Max-heap: highest credit first
    val debtor   = debtors.poll()   // Min-heap: most negative first

    val creditAmount = creditor.second
    val debitAmount  = debtor.second.abs()
    val settlementAmount = creditAmount.min(debitAmount)

    result.add(SimplifiedTransaction(from = debtorUser,
                                     to   = creditorUser,
                                     amount = settlementAmount))

    val remainingCredit = creditAmount - settlementAmount
    val remainingDebit  = (debitAmount - settlementAmount).negate()

    if (remainingCredit > BigDecimal("0.01")) creditors.add(...)
    if (remainingDebit  < BigDecimal("-0.01")) debtors.add(...)
}
```

**Example:**

| Member | Paid | Owes | Net Balance |
|---|---|---|---|
| Hamza | Rs. 600 | Rs. 200 | +Rs. 400 (creditor) |
| Asad | Rs. 100 | Rs. 300 | -Rs. 200 (debtor) |
| Bilal | Rs. 50 | Rs. 250 | -Rs. 200 (debtor) |

**Output (2 transactions, not 6):**
- Asad → Hamza: Rs. 200
- Bilal → Hamza: Rs. 200

---

### 11.2 Mess Bill Calculation Algorithm

**Class:** `CalculateMessBillUseCase.kt`  
**Location:** `domain/usecase/`

**Formula:**

Each meal type carries a configurable weight (e.g., Breakfast = 0.5, Lunch = 1.0, Dinner = 1.0):

```
WeightedUnits(user) = (BreakfastCount × BreakfastWeight)
                    + (LunchCount    × LunchWeight)
                    + (DinnerCount   × DinnerWeight)

TotalUnits = Σ WeightedUnits(all users)

CostPerUnit = TotalGroceryCost ÷ TotalUnits

Bill(user) = CostPerUnit × WeightedUnits(user)
```

**Safety:** If `TotalUnits == 0` (no meals logged), the algorithm returns early with zero bills to prevent a division-by-zero exception. This check uses `compareTo(BigDecimal.ZERO) == 0` to handle BigDecimal scale differences correctly.

**Example:**

Total Grocery Cost: Rs. 3,000  
Weights: Breakfast = 0.5, Lunch = 1.0, Dinner = 1.0

| Member | Breakfasts | Lunches | Dinners | Weighted Units | Bill |
|---|---|---|---|---|---|
| Hamza | 20 | 25 | 28 | 10 + 25 + 28 = **63** | Rs. 1,575 |
| Asad | 15 | 20 | 15 | 7.5 + 20 + 15 = **42.5** | Rs. 1,062.5 |
| **Total** | | | | **105.5 units** | **Rs. 3,000** |

---

### 11.3 Reactive Balance Calculation

**Method:** `ExpenseRepositoryImpl.observeNetBalances()`

The balance observable uses Kotlin's `combine` operator to merge four independent Room Flows into a single computed balance stream:

```kotlin
combine(
    expenseDao.observeTotalPaidPerUser(groupId),          // Flow 1
    expenseDao.observeTotalOwedPerUser(groupId),          // Flow 2
    settlementDao.observeTotalPaidSettlementsPerUser(groupId),     // Flow 3
    settlementDao.observeTotalReceivedSettlementsPerUser(groupId)  // Flow 4
) { paidRows, owedRows, paidSetRows, receivedSetRows ->

    // For each user:
    TotalActuallyPaid = ExpensesPaid + SettlementsPaid
    TotalActuallyOwed = ExpensesOwed + SettlementsReceived
    NetBalance        = TotalActuallyPaid - TotalActuallyOwed
}
```

Whenever any of these four data sources change (due to a new expense, participant, or settlement record), Room automatically triggers the corresponding Flow, and the `combine` operator re-emits a fresh `List<Balance>` to the ViewModel — with no manual refresh needed.

---

## 12. Implementation Details

### 12.1 Project Package Structure

```
com.khata.app/
├── KhataApplication.kt          ← Hilt Application entry point
├── MainActivity.kt              ← Single Activity host for Compose
│
├── core/
│   ├── di/                      ← Hilt Dependency Injection modules
│   ├── navigation/              ← Type-safe NavGraph & Screen sealed class
│   ├── security/                ← Database passphrase, biometric helpers
│   ├── theme/                   ← Material 3 color tokens, typography
│   └── utils/                   ← Extension functions, formatters
│
├── data/
│   ├── local/
│   │   ├── KhataDatabase.kt     ← Room DB configuration + SQLCipher hook
│   │   ├── dao/                 ← 5 DAOs (Expense, User, Group, Settlement, Meal)
│   │   ├── entity/              ← 7 Room Entity data classes
│   │   ├── converter/           ← TypeConverters (BigDecimal↔TEXT, enums)
│   │   └── mapper/              ← Entity-to-Domain model mappers
│   └── repository/              ← Repository implementations
│
├── domain/
│   ├── algorithm/
│   │   └── DebtSimplifier.kt    ← O(N log N) greedy settlement algorithm
│   ├── model/                   ← Pure Kotlin domain models (no Android deps)
│   ├── repository/              ← Repository interface contracts
│   └── usecase/                 ← 3 Use Cases (Expense, MessBill, SimplifyDebts)
│
└── presentation/
    ├── dashboard/               ← Home screen + ViewModel
    ├── expense/                 ← Expense list & add screen + ViewModel
    ├── group/                   ← Members screen + ViewModel
    ├── meal/                    ← Mess tracker screen + ViewModel
    ├── onboarding/              ← First-run setup screen + ViewModel
    ├── settings/                ← Settings + ViewModel
    └── settlement/              ← Settle up screen + ViewModel
```

### 12.2 Security Implementation

**Database Encryption:**
The `KhataDatabase` is initialized with a SQLCipher `SupportFactory`. The encryption passphrase is generated once, stored in `EncryptedSharedPreferences` (itself backed by the Android Keystore), and used on every subsequent database open.

```kotlin
// Passphrase stored in EncryptedSharedPreferences
val passphrase = SecurityUtils.getOrCreateDbPassphrase(context)
val factory = SupportFactory(passphrase)
Room.databaseBuilder(context, KhataDatabase::class.java, "khata.db")
    .openHelperFactory(factory)
    .build()
```

This ensures that even if the device's storage is physically extracted, the database file cannot be read without the passphrase stored in the hardware-backed Android Keystore.

### 12.3 State Management

Each screen has a dedicated ViewModel that:
1. Exposes UI state via `StateFlow<UiState>`.
2. Collects from repository `Flow`s using `viewModelScope`.
3. Handles all loading, success, and error states.

```kotlin
// Example: DashboardViewModel balance observation
viewModelScope.launch {
    expenseRepository.observeNetBalances(groupId)
        .collect { result ->
            _uiState.update { state ->
                when (result) {
                    is Result.Success -> state.copy(balances = result.data)
                    is Result.Loading -> state.copy(isLoading = true)
                    is Result.Error   -> state.copy(error = result.message)
                }
            }
        }
}
```

---

## 13. Application Screenshots

The following screenshots demonstrate the implemented application features:

### Screen 1 — Dashboard (Home)

> *The main dashboard showing the "Your Balance" card with real-time Net Balance, You Paid, and You Owe figures. Below it are quick-access buttons for Expenses, Meals, and Members, followed by the Recent Expenses list.*

*(Refer to attached screenshot: dashboard.png)*

Key elements visible:
- **Your Balance Card**: Shows `Rs. 0` Net Balance, `You Paid: Rs. 270`, `You Owe: Rs. 270`
- **Recent Expense**: "roti — Paid by Hamza — Rs. 270"
- Group selector: "My Hostel Room"

---

### Screen 2 — Add Expense

> *The expense creation form showing the category picker, split type tabs (Equal / Exact / Percentage), and the participant checklist with calculated shares.*

*(Refer to attached screenshot: add_expense.png)*

Key elements visible:
- Description and Amount input fields
- Category chips: Food, Utilities, Rent, Transport
- Split types: **Equal**, Exact, Percentage (tab navigation)
- Participants list with checkboxes and auto-calculated share amounts

---

### Screen 3 — Members Screen

> *The group info and member list screen showing the user ("Hamza — You") and a second member ("Asad") with their room number and phone number.*

*(Refer to attached screenshot: members.png)*

Key elements visible:
- Group Information card (description, currency)
- Member avatars with initial letters and color coding
- "(You)" label on the current user
- "Add Member" floating action button

---

### Screen 4 — Mess Tracker

> *The Meal Tracker showing the June 2026 calendar grid, a Monthly Bill Calculator with a grocery input field, and a Daily Meal Logs section at the bottom.*

*(Refer to attached screenshot: mess_tracker.png)*

Key elements visible:
- Interactive calendar heat-map (Mon–Sun layout)
- Monthly Bill Calculator with grocery cost input
- Rate per unit and Total Units calculated dynamically
- "Show per Member Bill" action

---

## 14. Testing & Validation

### 14.1 Manual Testing Matrix

| Feature | Test Case | Expected Result | Status |
|---|---|---|---|
| Add Expense (Equal Split) | Add Rs. 270 shared by 2 members | Each owes Rs. 135 | ✅ PASS |
| Add Expense (Exact Split) | Assign Rs. 200 to Hamza, Rs. 70 to Asad | Exact amounts stored | ✅ PASS |
| Settle Up | Asad marks Rs. 135 as settled | Net balance updates to Rs. 0 | ✅ PASS |
| Mess Bill Calculation | 30 lunches × Rs. 3000 grocery | Proportional bill computed | ✅ PASS |
| Debt Simplifier | 3 members with complex debts | Minimum 2 transactions | ✅ PASS |
| Real-time Balance Update | Add new expense from same session | Dashboard balance updates instantly | ✅ PASS |
| Process Kill Recovery | Kill app, return to Meal Tracker | No crash; state restored | ✅ PASS |
| Division by Zero Guard | Open Mess Tracker with no meal logs | Returns zero-bill result safely | ✅ PASS |
| Profile Edit | Change name in Settings | New name reflected in all screens | ✅ PASS |
| Recent Expense Navigation | Tap recent expense on dashboard | Opens expense list for the group | ✅ PASS |

### 14.2 Edge Cases Handled

| Edge Case | Handling Strategy |
|---|---|
| `BigDecimal` scale mismatch causing false equality | Used `compareTo(BigDecimal.ZERO) == 0` instead of `==` |
| Null `groupId` on process-kill restoration | Null-safe extraction with `?: ""` fallback |
| Empty expense list showing blank screen | Scaffold `PaddingValues` applied to all UI states |
| Division by zero in mess bill | Early return of empty `MessBillResult` when total units = 0 |
| Settlement not updating dashboard balance | Upgraded `observeNetBalances` to 4-way `combine` including settlement flows |

---

## 15. Challenges Faced & Solutions

### Challenge 1: Real-Time Balance Accuracy After Settlements
**Problem:** Adding a "Settle Up" transaction was correctly saved to the database but the Dashboard balance card did not update. The "You owe" figure remained stale.

**Root Cause:** The `observeNetBalances` Flow was only combining expense streams. Settlement data was not included in the reactive pipeline.

**Solution:** Refactored `SettlementDao` to expose reactive `Flow`-based queries. Updated `observeNetBalances` to use a 4-way Kotlin `combine` operator, merging both expense and settlement streams. Any write to either table now automatically triggers a balance recalculation.

---

### Challenge 2: App Crash in Meal Tracker on Process Kill
**Problem:** The application crashed with a `NullPointerException` when navigating to the Meal Tracker after Android killed the app process in the background.

**Root Cause:** The ViewModel used `checkNotNull(savedStateHandle["groupId"])` which threw an exception when the saved state was not available during restoration.

**Solution:** Replaced `checkNotNull` with a null-safe extraction pattern using the Elvis operator (`?: ""`), allowing the screen to load with a safe default state while the parent navigation re-provides the correct argument.

---

### Challenge 3: Division by Zero in Mess Bill Calculator
**Problem:** The app crashed with `ArithmeticException: Division by zero` when a user opened the Meal Tracker before logging any meals.

**Root Cause:** The guard condition `if (totalUnitsAllUsers == BigDecimal.ZERO)` was failing. `BigDecimal.ZERO` has scale 0, while a freshly computed `BigDecimal("0.0")` has scale 1. The `==` operator checks both value AND scale, causing the guard to be bypassed.

**Solution:** Changed the equality check to `totalUnitsAllUsers.compareTo(BigDecimal.ZERO) == 0`, which compares numerical value only, correctly identifying zero regardless of scale.

---

### Challenge 4: Gradle Build Toolchain Conflicts
**Problem:** Persistent `compileDebugJavaWithJavac` errors caused by multiple Gradle daemons using different JDK versions simultaneously.

**Solution:** Used `./gradlew --stop` to terminate all orphaned daemon processes, then rebuilt with `--no-daemon` to force a clean, single-daemon build under the correct JDK 17 toolchain.

---

## 16. Future Enhancements

| Enhancement | Description | Priority |
|---|---|---|
| **Cloud Sync** | Firebase/Supabase integration for cross-device data sharing | High |
| **OCR Receipt Scanning** | Use ML Kit to automatically extract amounts from grocery receipts | Medium |
| **Analytics Dashboard** | Monthly spending charts (Vico library already integrated) | Medium |
| **EasyPaisa/JazzCash Deep Links** | One-tap payment initiation using phone numbers stored in member profiles | Medium |
| **Urdu Language Support** | Full RTL layout and Urdu string resources | Low |
| **Recurring Expenses** | Auto-create recurring expenses (e.g., monthly rent) | Low |
| **Export to PDF/Excel** | Generate a downloadable expense report for a given month | Low |

---

## 17. Conclusion

**Khata** successfully delivers a complete, production-quality Android application that addresses the real financial management needs of hostel students. The application demonstrates proficient application of modern Android development practices including:

- **Clean Architecture** with strict separation of concerns
- **Reactive Programming** using Kotlin Coroutines and Flow for live UI updates
- **High-Precision Financial Math** using BigDecimal to eliminate rounding errors
- **O(N log N) Algorithm Design** for optimal debt simplification
- **Security Engineering** with AES-256 database encryption

All critical features — real-time balance tracking, fair mess bill calculation, and minimal-transaction debt settlement — have been implemented, tested, and validated. The application addresses the shortcomings of existing solutions (internet dependency, lack of meal tracking, no local encryption) and provides a reliable tool tailored for the Pakistani hostel context.

The project provided hands-on experience with production-level Android architecture, reactive data pipelines, financial algorithm implementation, and secure mobile application development.

---

## 18. References

1. Google. (2024). *Jetpack Compose Documentation*. Android Developers. https://developer.android.com/jetpack/compose

2. Google. (2024). *Room Persistence Library Documentation*. Android Developers. https://developer.android.com/training/data-storage/room

3. JetBrains. (2024). *Kotlin Coroutines & Flow Guide*. https://kotlinlang.org/docs/coroutines-guide.html

4. Zetetic LLC. (2024). *SQLCipher for Android*. https://www.zetetic.net/sqlcipher/sqlcipher-for-android/

5. Google. (2024). *Hilt Dependency Injection for Android*. https://developer.android.com/training/dependency-injection/hilt-android

6. Martin, R. C. (2012). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.

7. Knuth, D. E. (1998). *The Art of Computer Programming, Volume 3: Sorting and Searching (2nd ed.)*. Addison-Wesley. [Priority Queue / Heap operations]

8. Google. (2024). *Material Design 3 Guidelines*. https://m3.material.io/

9. Google. (2024). *Android Security Best Practices*. Android Developers. https://developer.android.com/privacy-and-security/security-tips

10. Martini, P. (2024). *Vico Chart Library for Android*. https://patrykandpatrick.com/vico/wiki/

---

*This documentation was prepared as part of the academic project submission for [Course Name], [Semester], [University Name].*

*All source code is original work. GitHub Repository: [Insert Link]*

---
