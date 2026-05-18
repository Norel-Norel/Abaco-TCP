package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.osnordev.abaco.domain.repository.ActiveClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActiveClientRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ActiveClientRepository {

    private val key = longPreferencesKey("active_client_id")

    override fun getActiveClientId(): Flow<Long?> =
        dataStore.data.map { it[key] }

    override suspend fun setActiveClientId(id: Long) {
        dataStore.edit { it[key] = id }
    }

    override suspend fun clearActiveClientId() {
        dataStore.edit { it.remove(key) }
    }
}
