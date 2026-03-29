package com.osnordev.abaco.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.ContactEntity
import com.osnordev.abaco.data.local.ContactType
import com.osnordev.abaco.data.repository.ContactRepository
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.usecase.GetContactHistoryUseCase
import com.osnordev.abaco.domain.usecase.InsertContactUseCase
import com.osnordev.abaco.domain.usecase.SearchContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactDetailState(
    val contact: ContactEntity? = null,
    val history: List<Transaction> = emptyList(),
    val netBalance: Double = 0.0
)

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: ContactRepository,
    private val insertUseCase: InsertContactUseCase,
    private val searchUseCase: SearchContactsUseCase,
    private val historyUseCase: GetContactHistoryUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: StateFlow<List<ContactEntity>> = _query
        .flatMapLatest { searchUseCase(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _detail = MutableStateFlow(ContactDetailState())
    val detail: StateFlow<ContactDetailState> = _detail.asStateFlow()

    fun onQueryChange(q: String) = _query.update { q }

    fun loadDetail(contactId: Long) {
        viewModelScope.launch {
            val contact = repository.getById(contactId) ?: return@launch
            val history = historyUseCase(contactId)
            val net = history.sumOf { if (it.contactId == contactId) it.amountCup else 0.0 }
            _detail.update { ContactDetailState(contact = contact, history = history, netBalance = net) }
        }
    }

    fun saveContact(name: String, phone: String, type: ContactType, notes: String) {
        viewModelScope.launch {
            insertUseCase(ContactEntity(name = name, phone = phone, type = type, notes = notes))
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
