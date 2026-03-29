package com.osnordev.abaco.ui.screens.taxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.model.TaxResult
import com.osnordev.abaco.domain.usecase.GetTaxResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaxViewModel @Inject constructor(
    private val getTaxResult: GetTaxResultUseCase
) : ViewModel() {

    private val _period = MutableStateFlow(run {
        val now = LocalDate.now()
        now.year to now.monthValue
    })

    val taxResult: StateFlow<TaxResult?> = _period
        .flatMapLatest { (year, month) -> getTaxResult(year, month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val period: StateFlow<Pair<Int, Int>> = _period

    fun setPeriod(year: Int, month: Int) {
        _period.update { year to month }
    }
}
