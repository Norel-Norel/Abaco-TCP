package com.osnordev.abaco.domain.repository

import com.osnordev.abaco.domain.model.TaxConfig
import kotlinx.coroutines.flow.Flow

interface TaxConfigRepository {
    fun getTaxConfig(): Flow<TaxConfig>
    suspend fun saveTaxConfig(config: TaxConfig)
}
