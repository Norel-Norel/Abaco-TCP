package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface OnboardingRepository {
    fun isCompleted(): Flow<Boolean>
    suspend fun markCompleted()
}

class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    private val key = booleanPreferencesKey("onboarding_completed")

    override fun isCompleted(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[key] ?: false }

    override suspend fun markCompleted() {
        dataStore.edit { prefs -> prefs[key] = true }
    }
}
