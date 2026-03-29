package com.osnordev.abaco.domain.repository

import com.osnordev.abaco.domain.model.CurrencyConfig
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getCurrencyConfig(): Flow<CurrencyConfig>
    suspend fun saveCurrencyConfig(config: CurrencyConfig)
}
