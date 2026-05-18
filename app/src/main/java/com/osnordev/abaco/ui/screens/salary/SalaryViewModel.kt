package com.osnordev.abaco.ui.screens.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.PayrollDao
import com.osnordev.abaco.data.local.PayrollRecordEntity
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.PayrollCalculation
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import com.osnordev.abaco.domain.usecase.CalculatePayrollUseCase
import com.osnordev.abaco.domain.usecase.CreateSalaryJournalEntryUseCase
import com.osnordev.abaco.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val calculatePayrollUseCase: CalculatePayrollUseCase,
    private val createSalaryJournalEntryUseCase: CreateSalaryJournalEntryUseCase,
    private val taxConfigRepository: TaxConfigRepository,
    private val payrollDao: PayrollDao,
    private val currentClientManager: CurrentClientManager
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

    /** Historial filtrado por cliente activo */
    val payrollHistory: StateFlow<List<PayrollRecordEntity>> =
        currentClientManager.activeClientId
            .flatMapLatest { clientId ->
                if (clientId != null) payrollDao.getAllByClient(clientId)
                else payrollDao.getAll()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
                baseSalary = base,
                taxConfig = config
            )
        }
    }

    fun postToJournal() {
        val payroll = _payrollResult.value ?: return
        val base = _grossInput.value.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            val result = createSalaryJournalEntryUseCase(payroll)
            _saveStatus.value = result
            if (result is ValidationResult.Valid) {
                // Persistir en historial
                val now = LocalDate.now()
                val period = "${now.month.getDisplayName(TextStyle.SHORT, Locale("es"))} ${now.year}".uppercase()
                payrollDao.insert(
                    PayrollRecordEntity(
                        employeeName     = payroll.employeeName,
                        ci               = payroll.ci,
                        baseSalary       = base,
                        grossSalary      = payroll.grossSalary,
                        cssEmployee      = payroll.cssEmployee,
                        iipRetained      = payroll.iipRetained,
                        netSalary        = payroll.netSalary,
                        cssEmployer      = payroll.cssEmployer,
                        holidayProvision = payroll.holidayProvision,
                        subsidyProvision = payroll.subsidyProvision,
                        specialSS        = payroll.specialSS,
                        totalCompanyCost = payroll.totalCompanyCost,
                        period           = period,
                        clientId         = currentClientManager.activeClientId.value ?: 1L
                    )
                )
                // Limpiar formulario
                _employeeName.value = ""
                _ci.value = ""
                _grossInput.value = ""
                _payrollResult.value = null
            }
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch { payrollDao.delete(id) }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}
