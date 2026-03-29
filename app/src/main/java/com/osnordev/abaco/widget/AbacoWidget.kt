package com.osnordev.abaco.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.osnordev.abaco.MainActivity
import com.osnordev.abaco.R
import com.osnordev.abaco.data.local.AppDatabase
import com.osnordev.abaco.domain.model.TransactionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Home screen widget showing income, expenses and net income for the current period.
 * Requirements: 21.1, 21.2, 21.3, 21.4
 */
@AndroidEntryPoint
class AbacoWidget : AppWidgetProvider() {

    @Inject
    lateinit var database: AppDatabase

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_abaco)

        // Quick action: open TransactionFormScreen (Req 21.2)
        val addIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_ADD_TRANSACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val addPendingIntent = PendingIntent.getActivity(
            context, 0, addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_add_button, addPendingIntent)

        // Load current period totals asynchronously (Req 21.1)
        CoroutineScope(Dispatchers.IO).launch {
            val now = LocalDate.now()
            val transactions = database.transactionDao().getAllSync()
                .filter { it.year == now.year && it.month == now.monthValue }

            val income = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountCup }
            val expenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountCup }
            val net = income - expenses

            views.setTextViewText(R.id.widget_income, "%.0f".format(income))
            views.setTextViewText(R.id.widget_expenses, "%.0f".format(expenses))
            views.setTextViewText(R.id.widget_net, "%.0f".format(net))

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    companion object {
        const val ACTION_ADD_TRANSACTION = "com.osnordev.abaco.ADD_TRANSACTION"

        /**
         * Triggers a widget update from anywhere in the app (Req 21.3, 21.4).
         */
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, AbacoWidget::class.java)
            )
            if (ids.isNotEmpty()) {
                val intent = Intent(context, AbacoWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
