package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.repository.PeriodRepository
import kotlinx.coroutines.flow.first
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeriodRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val currentClientManager: CurrentClientManager
) : PeriodRepository {

    override suspend fun isPeriodClosed(year: Int, month: Int): Boolean {
        val clientId = currentClientManager.activeClientId.value ?: 1L
        val prefs = dataStore.data.first()
        val closed = prefs[stringSetPreferencesKey("closed_periods_$clientId")] ?: emptySet()
        val label = "${Month.of(month).getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercase() }} $year"
        return label in closed
    }
}
