package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.osnordev.abaco.domain.model.TaxConfig
import com.osnordev.abaco.domain.model.defaultIipBrackets
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class TaxConfigRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TaxConfigRepository {

    private val taxConfigKey = stringPreferencesKey("tax_config")

    override fun getTaxConfig(): Flow<TaxConfig> =
        dataStore.data.map { prefs ->
            val json = prefs[taxConfigKey]
            if (json != null) {
                try {
                    Json.decodeFromString<TaxConfig>(json)
                } catch (e: Exception) {
                    TaxConfig()
                }
            } else {
                TaxConfig()
            }
        }

    override suspend fun saveTaxConfig(config: TaxConfig) {
        dataStore.edit { prefs ->
            prefs[taxConfigKey] = Json.encodeToString(config)
        }
    }
}
