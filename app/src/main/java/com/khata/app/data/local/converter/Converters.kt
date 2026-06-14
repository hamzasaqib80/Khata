package com.khata.app.data.local.converter

import androidx.room.TypeConverter
import java.math.BigDecimal

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}

class BigDecimalConverter {
    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? {
        return value?.toPlainString()
    }
}

class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}
