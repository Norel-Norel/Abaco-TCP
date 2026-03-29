package com.osnordev.abaco.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.osnordev.abaco.data.local.PaymentDueEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_PAYMENT_ID = "payment_id"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_DUE_DATE = "due_date"
    }

    /** Schedules alarms 1 day before and on the due date. Returns (alarmId1, alarmId2). */
    fun schedule(payment: PaymentDueEntity): Pair<Int, Int> {
        val alarmId1 = (payment.id * 2).toInt()
        val alarmId2 = (payment.id * 2 + 1).toInt()

        val dayBefore = payment.dueDate.minusDays(1)
        if (!dayBefore.isBefore(LocalDate.now())) {
            scheduleAlarm(alarmId1, dayBefore, payment)
        }
        if (!payment.dueDate.isBefore(LocalDate.now())) {
            scheduleAlarm(alarmId2, payment.dueDate, payment)
        }
        return alarmId1 to alarmId2
    }

    fun cancel(alarmId1: Int?, alarmId2: Int?) {
        listOfNotNull(alarmId1, alarmId2).forEach { id ->
            val intent = buildIntent(id, "", 0.0, "")
            alarmManager.cancel(intent)
        }
    }

    private fun scheduleAlarm(alarmId: Int, date: LocalDate, payment: PaymentDueEntity) {
        val triggerMs = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() +
                9 * 60 * 60 * 1000L  // 9:00 AM

        val pendingIntent = buildIntent(
            alarmId,
            payment.description,
            payment.amount,
            payment.dueDate.toString()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    private fun buildIntent(
        alarmId: Int,
        description: String,
        amount: Double,
        dueDate: String
    ): PendingIntent {
        val intent = Intent(context, PaymentNotificationReceiver::class.java).apply {
            putExtra(EXTRA_PAYMENT_ID, alarmId)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(EXTRA_DUE_DATE, dueDate)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
