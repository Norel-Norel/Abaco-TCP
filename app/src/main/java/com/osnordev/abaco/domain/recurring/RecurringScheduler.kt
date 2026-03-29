package com.osnordev.abaco.domain.recurring

import com.osnordev.abaco.data.local.RecurringFrequency
import com.osnordev.abaco.data.local.RecurringTemplateEntity
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringScheduler @Inject constructor() {

    /**
     * Returns the next occurrence date after [template.nextDate] based on its frequency.
     * Requirements: 20.1, 20.2
     */
    fun nextOccurrence(template: RecurringTemplateEntity): LocalDate =
        advance(template.nextDate, template.frequency)

    /**
     * Advances [from] by exactly one unit of [frequency].
     */
    fun advance(from: LocalDate, frequency: RecurringFrequency): LocalDate = when (frequency) {
        RecurringFrequency.DAILY     -> from.plusDays(1)
        RecurringFrequency.WEEKLY    -> from.plusWeeks(1)
        RecurringFrequency.BIWEEKLY  -> from.plusWeeks(2)
        RecurringFrequency.MONTHLY   -> from.plusMonths(1)
    }
}
