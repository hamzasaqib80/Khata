package com.khata.app.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class User(
    val id: String,
    val name: String,
    val avatarColorHex: String,
    val phoneNumber: String?,
    val roomNo: String?,
    val isCurrentUser: Boolean
)

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val isRtl: Boolean = false
) {
    PKR("PKR", "Rs.", "Pakistani Rupee"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    SAR("SAR", "SR", "Saudi Riyal", isRtl = true),
    AED("AED", "AED", "UAE Dirham", isRtl = true)
}

data class Group(
    val id: String,
    val name: String,
    val description: String?,
    val currency: Currency,
    val members: List<User>,
    val totalExpenses: BigDecimal
)

enum class SplitType { EQUAL, UNEQUAL, PERCENTAGE }

enum class ExpenseCategory { FOOD, UTILITIES, RENT, TRANSPORT, GROCERIES, OTHER }

data class Expense(
    val id: String,
    val groupId: String,
    val payer: User,
    val description: String,
    val amount: BigDecimal,
    val currency: Currency,
    val splitType: SplitType,
    val category: ExpenseCategory,
    val participants: List<ExpenseSplit>,
    val date: LocalDate,
    val receiptNote: String?
)

data class ExpenseSplit(
    val user: User,
    val shareAmount: BigDecimal,
    val sharePercentage: BigDecimal?
)

data class Balance(
    val user: User,
    val totalPaid: BigDecimal,
    val totalOwed: BigDecimal,
    val netBalance: BigDecimal // positive = is owed money, negative = owes money
)

data class SimplifiedTransaction(
    val from: User, // debtor (payer)
    val to: User,   // creditor (receiver)
    val amount: BigDecimal,
    val currency: Currency
)

data class MealPlan(
    val id: String,
    val groupId: String,
    val month: Int,
    val year: Int,
    val totalGroceryCost: BigDecimal,
    val breakfastWeight: BigDecimal,
    val lunchWeight: BigDecimal,
    val dinnerWeight: BigDecimal
)

data class MealLog(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val hadBreakfast: Boolean,
    val hadLunch: Boolean,
    val hadDinner: Boolean
)

data class MessBillResult(
    val perUserBill: Map<User, BigDecimal>,
    val totalUnits: BigDecimal,
    val costPerUnit: BigDecimal,
    val breakdown: Map<User, UserMealBreakdown>
)

data class UserMealBreakdown(
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int,
    val totalWeightedUnits: BigDecimal,
    val billAmount: BigDecimal
)

data class UserMealCounts(
    val userId: String,
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int
)
