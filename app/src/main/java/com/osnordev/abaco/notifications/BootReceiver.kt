package com.osnordev.abaco.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.osnordev.abaco.data.repository.PaymentDueRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var paymentDueRepository: PaymentDueRepository
    @Inject lateinit var scheduler: PaymentNotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val pending = paymentDueRepository.getPending().first()
            val today = LocalDate.now()
            pending
                .filter { !it.dueDate.isBefore(today) }
                .forEach { payment ->
                    val (id1, id2) = scheduler.schedule(payment)
                    paymentDueRepository.updateAlarmIds(payment.id, id1, id2)
                }
        }
    }
}
