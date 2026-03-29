package com.osnordev.abaco.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.osnordev.abaco.data.local.RecurringTemplateDao
import com.osnordev.abaco.domain.model.Currency
import com.osnordev.abaco.domain.model.SyncStatus
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.recurring.RecurringScheduler
import com.osnordev.abaco.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * Runs daily via WorkManager. For each active recurring template whose nextDate
 * is today or earlier, creates a transaction and advances the template's nextDate.
 * Requirements: 20.2
 */
@HiltWorker
class RecurringWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recurringTemplateDao: RecurringTemplateDao,
    private val transactionRepository: TransactionRepository,
    private val scheduler: RecurringScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val due = recurringTemplateDao.getDue(today.toString())

        due.forEach { template ->
            val tx = Transaction(
                type = template.type,
                amount = template.amount,
                category = template.category,
                description = template.description,
                date = template.nextDate,
                year = template.nextDate.year,
                month = template.nextDate.monthValue,
                currency = Currency.valueOf(template.currency),
                amountCup = template.amount,
                isRecurring = true,
                recurringId = template.id,
                syncStatus = SyncStatus.PENDING
            )
            transactionRepository.insert(tx)

            // Advance nextDate by one frequency unit
            val updated = template.copy(nextDate = scheduler.nextOccurrence(template))
            recurringTemplateDao.update(updated)
        }

        return Result.success()
    }
}
