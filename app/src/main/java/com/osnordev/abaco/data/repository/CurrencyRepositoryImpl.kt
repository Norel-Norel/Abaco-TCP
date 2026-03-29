package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.osnordev.abaco.domain.model.CurrencyConfig
import com.osnordev.abaco.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CurrencyRepository {

    private val key = stringPreferencesKey("currency_config")

    override fun getCurrencyConfig(): Flow<CurrencyConfig> =
        dataStore.data.map { prefs ->
            prefs[key]?.let {
                try { Json.decodeFromString(it) } catch (e: Exception) { CurrencyConfig() }
            } ?: CurrencyConfig()
        }

    override suspend fun saveCurrencyConfig(config: CurrencyConfig) {
        dataStore.edit { prefs -> prefs[key] = Json.encodeToString(config) }
    }
}
