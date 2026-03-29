package com.osnordev.abaco.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.model.AppModule
import com.osnordev.abaco.domain.model.CurrencyConfig
import com.osnordev.abaco.domain.model.TaxBracket
import com.osnordev.abaco.domain.model.TaxConfig
import com.osnordev.abaco.domain.repository.CurrencyRepository
import com.osnordev.abaco.domain.repository.ModuleRepository
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import com.osnordev.abaco.domain.validation.validateCssRate
import com.osnordev.abaco.domain.validation.validateIipBrackets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val taxConfig: TaxConfig = TaxConfig(),
    val moduleStates: Map<AppModule, Boolean> = AppModule.entries.associateWith { true },
    val cssRateInput: String = "20",
    val cssRateError: String? = null,
    val bracketsError: String? = null,
    val bracketRateErrors: Map<Int, String> = emptyMap(),
    val isSaved: Boolean = false,
    // Currency config
    val mlcToCupInput: String = "1.0",
    val usdToCupInput: String = "1.0",
    val currencyError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val taxConfigRepository: TaxConfigRepository,
    private val moduleRepository: ModuleRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    // Derived state from repositories
    val taxConfig: StateFlow<TaxConfig> = taxConfigRepository.getTaxConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaxConfig()
        )

    val moduleStates: StateFlow<Map<AppModule, Boolean>> = moduleRepository.getModuleStates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppModule.entries.associateWith { true }
        )

    init {
        // Sync repository data into editable UI state
        viewModelScope.launch {
            combine(taxConfigRepository.getTaxConfig(), moduleRepository.getModuleStates()) { config, modules ->
                config to modules
            }.collect { (config, modules) ->
                _uiState.update { current ->
                    current.copy(
                        taxConfig = config,
                        moduleStates = modules,
                        cssRateInput = (config.cssRate * 100).toBigDecimal().stripTrailingZeros().toPlainString()
                    )
                }
            }
        }
        viewModelScope.launch {
            currencyRepository.getCurrencyConfig().collect { config ->
                _uiState.update { it.copy(
                    mlcToCupInput = config.mlcToCup.toString(),
                    usdToCupInput = config.usdToCup.toString()
                ) }
            }
        }
    }

    // --- CSS Rate ---

    fun onCssRateInputChange(input: String) {
        val error = if (input.isNotBlank()) validateCssRate(input.toDoubleOrNull()) else null
        _uiState.update { it.copy(cssRateInput = input, cssRateError = error, isSaved = false) }
    }

    // --- IIP Brackets ---

    fun onBracketRateChange(index: Int, rateInput: String) {
        val ratePercent = rateInput.toDoubleOrNull()
        // Validate inline: rate must be in [0, 100] as a percentage
        val rateError = if (ratePercent == null) {
            "Tasa inválida"
        } else if (ratePercent < 0.0 || ratePercent > 100.0) {
            "La tasa debe estar entre 0 y 100"
        } else null

        val newRateErrors = _uiState.value.bracketRateErrors.toMutableMap()
        if (rateError != null) {
            newRateErrors[index] = rateError
        } else {
            newRateErrors.remove(index)
        }

        if (ratePercent != null) {
            val brackets = _uiState.value.taxConfig.iipBrackets.toMutableList()
            brackets[index] = brackets[index].copy(rate = ratePercent / 100.0)
            _uiState.update {
                it.copy(
                    taxConfig = it.taxConfig.copy(iipBrackets = brackets),
                    bracketRateErrors = newRateErrors,
                    bracketsError = null,
                    isSaved = false
                )
            }
        } else {
            _uiState.update { it.copy(bracketRateErrors = newRateErrors, isSaved = false) }
        }
    }

    fun onBracketToChange(index: Int, toInput: String) {
        val brackets = _uiState.value.taxConfig.iipBrackets.toMutableList()
        val to = if (toInput.isBlank()) null else toInput.toDoubleOrNull() ?: return
        brackets[index] = brackets[index].copy(to = to)
        _uiState.update {
            it.copy(
                taxConfig = it.taxConfig.copy(iipBrackets = brackets),
                bracketsError = null,
                isSaved = false
            )
        }
    }

    // --- Module toggles ---

    fun onModuleToggle(module: AppModule, enabled: Boolean) {
        viewModelScope.launch {
            moduleRepository.setModuleEnabled(module, enabled)
        }
    }

    // --- Save ---

    fun saveTaxConfig() {
        val state = _uiState.value

        // Validate CSS rate
        val cssPercent = state.cssRateInput.toDoubleOrNull()
        val cssError = validateCssRate(cssPercent)
        if (cssError != null) {
            _uiState.update { it.copy(cssRateError = cssError) }
            return
        }

        // Block save if any per-bracket rate errors exist
        if (state.bracketRateErrors.isNotEmpty()) {
            _uiState.update { it.copy(bracketsError = "Corrija los errores en las tasas de los tramos") }
            return
        }

        // Validate IIP brackets (consecutiveness + rates)
        val bracketsError = validateIipBrackets(state.taxConfig.iipBrackets)
        if (bracketsError != null) {
            _uiState.update { it.copy(bracketsError = bracketsError) }
            return
        }

        val newConfig = state.taxConfig.copy(cssRate = cssPercent!! / 100.0)
        viewModelScope.launch {
            taxConfigRepository.saveTaxConfig(newConfig)
            _uiState.update { it.copy(isSaved = true, cssRateError = null, bracketsError = null) }
        }
    }

    fun resetSavedFlag() {
        _uiState.update { it.copy(isSaved = false) }
    }

    // --- Currency rates ---

    fun onMlcRateChange(input: String) {
        _uiState.update { it.copy(mlcToCupInput = input, currencyError = null) }
    }

    fun onUsdRateChange(input: String) {
        _uiState.update { it.copy(usdToCupInput = input, currencyError = null) }
    }

    fun saveCurrencyConfig() {
        val mlc = _uiState.value.mlcToCupInput.toDoubleOrNull()
        val usd = _uiState.value.usdToCupInput.toDoubleOrNull()
        if (mlc == null || mlc <= 0 || usd == null || usd <= 0) {
            _uiState.update { it.copy(currencyError = "Los tipos de cambio deben ser mayores que cero") }
            return
        }
        viewModelScope.launch {
            currencyRepository.saveCurrencyConfig(CurrencyConfig(mlcToCup = mlc, usdToCup = usd))
            _uiState.update { it.copy(isSaved = true, currencyError = null) }
        }
    }
}
