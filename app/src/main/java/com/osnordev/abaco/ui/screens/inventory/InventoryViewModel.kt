package com.osnordev.abaco.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.InventoryItemEntity
import com.osnordev.abaco.data.local.InventoryMovementEntity
import com.osnordev.abaco.data.local.InventoryMovementType
import com.osnordev.abaco.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class InventoryUiState(
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val items: StateFlow<List<InventoryItemEntity>> = _searchQuery
        .flatMapLatest { q ->
            if (q.isBlank()) repository.getAllItems()
            else repository.searchItems(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItemEntity>> = repository.getLowStockItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getMovementsForItem(itemId: Long) = repository.getMovementsForItem(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.saveItem(item)
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteItem(item: InventoryItemEntity) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun registerMovement(
        itemId: Long,
        type: InventoryMovementType,
        quantity: Double,
        note: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.registerMovement(
                    InventoryMovementEntity(
                        itemId = itemId,
                        type = type,
                        quantity = quantity,
                        note = note,
                        date = LocalDate.now()
                    )
                )
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    suspend fun getItemById(id: Long) = repository.getItemById(id)
}
