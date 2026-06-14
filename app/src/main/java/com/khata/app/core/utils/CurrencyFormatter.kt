package com.khata.app.core.utils

import com.khata.app.domain.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Utility for formatting and parsing monetary amounts.
 */
object CurrencyFormatter {

    /**
     * Formats a BigDecimal amount in the given currency with proper locale and symbol.
     * Example: PKR: "Rs. 1,500" | USD: "$12.50"
     */
    fun format(amount: BigDecimal, currency: Currency): String {
        val format = when (currency) {
            Currency.PKR -> {
                // Pakistan doesn't have a built-in Locale in Java sometimes, 
                // we manually prepend the symbol
                val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                return "Rs. ${numberFormat.format(amount.setScale(2, RoundingMode.HALF_UP))}"
            }
            Currency.USD -> NumberFormat.getCurrencyInstance(Locale.US)
            Currency.EUR -> NumberFormat.getCurrencyInstance(Locale.GERMANY)
            Currency.GBP -> NumberFormat.getCurrencyInstance(Locale.UK)
            Currency.SAR -> {
                val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                return "SR ${numberFormat.format(amount.setScale(2, RoundingMode.HALF_UP))}"
            }
            Currency.AED -> {
                val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                return "AED ${numberFormat.format(amount.setScale(2, RoundingMode.HALF_UP))}"
            }
        }
        
        // Ensure decimal places are correct for currencies that use NumberFormat.getCurrencyInstance
        format.currency = java.util.Currency.getInstance(currency.code)
        return format.format(amount.setScale(2, RoundingMode.HALF_UP))
    }

    /**
     * Formats an amount into a compact representation for charts/summaries.
     * Example: "Rs. 1.5K"
     */
    fun formatCompact(amount: BigDecimal, currency: Currency): String {
        return when {
            amount >= BigDecimal(1000000) -> {
                "${currency.symbol} ${(amount / BigDecimal(1000000)).setScale(1, RoundingMode.HALF_UP)}M"
            }
            amount >= BigDecimal(1000) -> {
                "${currency.symbol} ${(amount / BigDecimal(1000)).setScale(1, RoundingMode.HALF_UP)}K"
            }
            else -> format(amount, currency)
        }
    }

    /**
     * Safely parses a string input into a BigDecimal.
     */
    fun parse(input: String): BigDecimal? {
        return try {
            if (input.isBlank()) null else BigDecimal(input.trim())
        } catch (e: Exception) {
            null
        }
    }
}
