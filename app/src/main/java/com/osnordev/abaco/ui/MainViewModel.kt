package com.osnordev.abaco.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.repository.OnboardingRepository
import com.osnordev.abaco.data.repository.PinRepository
import com.osnordev.abaco.data.repository.ProfileRepository
import com.osnordev.abaco.data.repository.ThemeRepository
import com.osnordev.abaco.data.repository.UserProfile
import com.osnordev.abaco.domain.model.AppModule
import com.osnordev.abaco.domain.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    moduleRepository: ModuleRepository,
    private val onboardingRepository: OnboardingRepository,
    private val pinRepository: PinRepository,
    private val themeRepository: ThemeRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val activeModules = moduleRepository.getModuleStates()
        .map { states -> states.filterValues { it }.keys }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppModule.entries.toSet()
        )

    /** null = loading, false = not completed, true = completed */
    val onboardingCompleted = onboardingRepository.isCompleted()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** null = use system default, true = dark, false = light */
    val isDarkTheme = themeRepository.isDarkTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val isNotificationsEnabled = themeRepository.isNotificationsEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch { themeRepository.setDarkTheme(dark) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setNotificationsEnabled(enabled) }
    }

    val userProfile = profileRepository.getProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserProfile()
        )

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch { profileRepository.saveProfile(profile) }
    }

    /** Whether the PIN lock screen needs to be shown this session */
    val pinEnabled: Boolean get() = pinRepository.isPinEnabled()

    private val _pinUnlocked = MutableStateFlow(!pinRepository.isPinEnabled())
    val pinUnlocked = _pinUnlocked.asStateFlow()

    /** Emits true when the PIN setup dialog should be shown */
    private val _showPinSetup = MutableStateFlow(false)
    val showPinSetup = _showPinSetup.asStateFlow()

    fun onPinUnlocked() {
        _pinUnlocked.value = true
    }

    /**
     * Toggles PIN: if enabled → disables it immediately.
     * If disabled → signals the UI to show the PIN setup dialog.
     */
    fun togglePin() {
        if (pinRepository.isPinEnabled()) {
            pinRepository.clearPin()
            _pinUnlocked.value = true
        } else {
            _showPinSetup.value = true
        }
    }

    fun onPinSetupDismissed() {
        _showPinSetup.value = false
    }

    fun setupPin(pin: String) {
        pinRepository.setPin(pin)
        _showPinSetup.value = false
        // PIN is now enabled; next app launch will require it
    }

    fun completeOnboarding() {
        viewModelScope.launch { onboardingRepository.markCompleted() }
    }
}
