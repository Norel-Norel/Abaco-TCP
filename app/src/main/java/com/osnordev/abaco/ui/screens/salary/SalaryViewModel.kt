package com.osnordev.abaco.ui.screens.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.model.PayrollCalculation
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import com.osnordev.abaco.domain.usecase.CalculatePayrollUseCase
import com.osnordev.abaco.domain.usecase.CreateSalaryJournalEntryUseCase
import com.osnordev.abaco.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val calculatePayrollUseCase: CalculatePayrollUseCase,
    private val createSalaryJournalEntryUseCase: CreateSalaryJournalEntryUseCase,
    private val taxConfigRepository: TaxConfigRepository
) : ViewModel() {

    private val _employeeName = MutableStateFlow("")
    val employeeName: StateFlow<String> = _employeeName.asStateFlow()

    private val _ci = MutableStateFlow("")
    val ci: StateFlow<String> = _ci.asStateFlow()

    private val _grossInput = MutableStateFlow("")
    val grossInput: StateFlow<String> = _grossInput.asStateFlow()

    private val _payrollResult = MutableStateFlow<PayrollCalculation?>(null)
    val payrollResult: StateFlow<PayrollCalculation?> = _payrollResult.asStateFlow()

    private val _saveStatus = MutableStateFlow<ValidationResult?>(null)
    val saveStatus: StateFlow<ValidationResult?> = _saveStatus.asStateFlow()

    fun onEmployeeNameChange(newValue: String) {
        _employeeName.value = newValue
        updateCalculation()
    }

    fun onCiChange(newValue: String) {
        _ci.value = newValue
        updateCalculation()
    }

    fun onGrossInputChange(newValue: String) {
        if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
            _grossInput.value = newValue
            updateCalculation()
        }
    }

    private fun updateCalculation() {
        val base = _grossInput.value.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            val config = taxConfigRepository.getTaxConfig().first()
            _payrollResult.value = calculatePayrollUseCase(
                employeeName = _employeeName.value,
                ci = _ci.value,
                baseSalary = base,   // salario base — el use case suma el 9.09% internamente
                taxConfig = config
            )
        }
    }

    /**
     * Guarda el cálculo actual como un asiento contable en el módulo de Asientos.
     */
    fun postToJournal() {
        val payroll = _payrollResult.value ?: return
        viewModelScope.launch {
            val result = createSalaryJournalEntryUseCase(payroll)
            _saveStatus.value = result
            // Limpiar campos si el asiento se generó correctamente
            if (result is ValidationResult.Valid) {
                _employeeName.value = ""
                _ci.value = ""
                _grossInput.value = ""
                _payrollResult.value = null
            }
        }
    }
    
    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}
