package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.osnordev.abaco.domain.model.AppModule
import com.osnordev.abaco.domain.repository.ModuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ModuleRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ModuleRepository {

    // Todos los módulos están activos por defecto
    private fun moduleKey(module: AppModule) =
        booleanPreferencesKey("module_${module.name}")

    override fun getModuleStates(): Flow<Map<AppModule, Boolean>> =
        dataStore.data.map { prefs ->
            AppModule.entries.associateWith { module ->
                prefs[moduleKey(module)] ?: true
            }
        }

    override suspend fun setModuleEnabled(module: AppModule, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[moduleKey(module)] = enabled
        }
    }
}
