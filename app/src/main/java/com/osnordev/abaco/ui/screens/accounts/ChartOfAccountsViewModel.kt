package com.osnordev.abaco.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.ChartOfAccountDao
import com.osnordev.abaco.data.local.ChartOfAccountEntity
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartOfAccountsViewModel @Inject constructor(
    private val dao: ChartOfAccountDao,
    private val currentClientManager: CurrentClientManager
) : ViewModel() {

    /** Cuentas filtradas por cliente activo */
    val accounts: StateFlow<List<ChartOfAccountEntity>> =
        currentClientManager.activeClientId
            .flatMapLatest { clientId ->
                if (clientId != null) dao.getAllByClient(clientId)
                else dao.getAll()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addAccount(
        code: String,
        name: String,
        type: AccountType,
        nature: String,
        parentCode: String?
    ) {
        val clientId = currentClientManager.activeClientId.value ?: 1L
        viewModelScope.launch {
            dao.upsert(
                ChartOfAccountEntity(
                    code = code.trim(),
                    name = name.trim(),
                    type = type,
                    nature = nature,
                    parentCode = parentCode?.trim()?.ifBlank { null },
                    clientId = clientId
                )
            )
        }
    }

    fun deactivate(code: String) {
        viewModelScope.launch { dao.deactivate(code) }
    }
}
