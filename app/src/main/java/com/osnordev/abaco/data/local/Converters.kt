package com.osnordev.abaco.data.local

import androidx.room.TypeConverter
import com.osnordev.abaco.domain.model.AccountType
import com.osnordev.abaco.domain.model.TransactionType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromRecurringFrequency(freq: RecurringFrequency): String = freq.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency = RecurringFrequency.valueOf(value)

    @TypeConverter
    fun fromContactType(type: ContactType): String = type.name

    @TypeConverter
    fun toContactType(value: String): ContactType = ContactType.valueOf(value)
}
