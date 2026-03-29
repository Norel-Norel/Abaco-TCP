package com.osnordev.abaco.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.osnordev.abaco.R

class PaymentNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "payment_due_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val description = intent.getStringExtra(PaymentNotificationScheduler.EXTRA_DESCRIPTION) ?: "Pago"
        val amount = intent.getDoubleExtra(PaymentNotificationScheduler.EXTRA_AMOUNT, 0.0)
        val dueDate = intent.getStringExtra(PaymentNotificationScheduler.EXTRA_DUE_DATE) ?: ""
        val notifId = intent.getIntExtra(PaymentNotificationScheduler.EXTRA_PAYMENT_ID, 0)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vencimientos de pagos",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notificaciones de pagos próximos a vencer"
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pago próximo a vencer")
            .setContentText("$description — %.2f CUP — Vence: $dueDate".format(amount))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$description\nImporte: %.2f CUP\nFecha de vencimiento: $dueDate".format(amount)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(notifId, notification)
    }
}
