package com.osnordev.abaco.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val name: String = "",
    val businessName: String = "",
    val licenseNumber: String = ""
)

@Singleton
class ProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val nameKey = stringPreferencesKey("profile_name")
    private val businessKey = stringPreferencesKey("profile_business")
    private val licenseKey = stringPreferencesKey("profile_license")

    fun getProfile(): Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[nameKey] ?: "",
            businessName = prefs[businessKey] ?: "",
            licenseNumber = prefs[licenseKey] ?: ""
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[nameKey] = profile.name
            prefs[businessKey] = profile.businessName
            prefs[licenseKey] = profile.licenseNumber
        }
    }
}
