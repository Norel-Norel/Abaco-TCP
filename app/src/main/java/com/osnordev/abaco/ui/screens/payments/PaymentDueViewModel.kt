package com.osnordev.abaco.ui.screens.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.PaymentDueEntity
import com.osnordev.abaco.data.repository.PaymentDueRepository
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.Currency
import com.osnordev.abaco.domain.usecase.InsertPaymentDueUseCase
import com.osnordev.abaco.domain.usecase.MarkAsPaidUseCase
import com.osnordev.abaco.notifications.PaymentNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PaymentDueViewModel @Inject constructor(
    private val repository: PaymentDueRepository,
    private val insertUseCase: InsertPaymentDueUseCase,
    private val markAsPaidUseCase: MarkAsPaidUseCase,
    private val scheduler: PaymentNotificationScheduler,
    private val currentClientManager: CurrentClientManager
) : ViewModel() {

    val pendingPayments: StateFlow<List<PaymentDueEntity>> =
        currentClientManager.activeClientId
            .flatMapLatest { clientId ->
                val id = clientId ?: 1L
                repository.getPendingByClient(id)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addPayment(description: String, amount: Double, dueDate: LocalDate) {
        viewModelScope.launch {
            val entity = PaymentDueEntity(
                description = description,
                amount = amount,
                currency = Currency.CUP.name,
                dueDate = dueDate
            )
            val id = insertUseCase(entity)
            val inserted = entity.copy(id = id)
            val (alarmId1, alarmId2) = scheduler.schedule(inserted)
            repository.updateAlarmIds(id, alarmId1, alarmId2)
        }
    }

    fun markAsPaid(payment: PaymentDueEntity) {
        viewModelScope.launch {
            scheduler.cancel(payment.alarmId1, payment.alarmId2)
            markAsPaidUseCase(payment.id)
        }
    }
}
