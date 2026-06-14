package com.khata.app.core.utils

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Common extension functions.
 */

fun BigDecimal.isZero() = this.compareTo(BigDecimal.ZERO) == 0

fun BigDecimal.isPositive() = this.compareTo(BigDecimal.ZERO) > 0

fun BigDecimal.isNegative() = this.compareTo(BigDecimal.ZERO) < 0

fun String.toBigDecimalOrZero(): BigDecimal {
    return try {
        if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this)
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}

/**
 * Scales a BigDecimal for internal calculation (higher precision)
 */
fun BigDecimal.scaleInternal(): BigDecimal = this.setScale(10, RoundingMode.HALF_UP)

/**
 * Scales a BigDecimal for final storage/display (monetary precision)
 */
fun BigDecimal.scaleMoney(): BigDecimal = this.setScale(2, RoundingMode.HALF_UP)
