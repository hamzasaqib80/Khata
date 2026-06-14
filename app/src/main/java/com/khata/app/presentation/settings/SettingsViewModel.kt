package com.khata.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.security.SecurePreferences
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.User
import com.khata.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isBiometricEnabled: Boolean = false,
    val isDarkModeEnabled: Boolean = false,
    val useSystemTheme: Boolean = true,
    val appVersion: String = "1.0.0",
    // Profile fields
    val currentUser: User? = null,
    val isEditingProfile: Boolean = false,
    val profileNameInput: String = "",
    val profilePhoneInput: String = "",
    val profileRoomInput: String = "",
    val isSavingProfile: Boolean = false,
    val profileSaveError: String? = null
)

sealed class SettingsEvent {
    object ProfileSaved : SettingsEvent()
    data class ShowSnackbar(val message: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    companion object {
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SYSTEM_THEME = "system_theme"
    }

    init {
        loadSettings()
        observeCurrentUser()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                isBiometricEnabled = securePreferences.getBoolean(KEY_BIOMETRIC, false),
                isDarkModeEnabled = securePreferences.getBoolean(KEY_DARK_MODE, false),
                useSystemTheme = securePreferences.getBoolean(KEY_SYSTEM_THEME, true)
            )
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(currentUser = result.data) }
                }
            }
        }
    }

    // ── Toggle actions ───────────────────────────────────────────────────────
    fun toggleBiometric(enabled: Boolean) {
        securePreferences.putBoolean(KEY_BIOMETRIC, enabled)
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        securePreferences.putBoolean(KEY_DARK_MODE, enabled)
        _uiState.update { it.copy(isDarkModeEnabled = enabled) }
    }

    fun toggleSystemTheme(useSystem: Boolean) {
        securePreferences.putBoolean(KEY_SYSTEM_THEME, useSystem)
        _uiState.update { it.copy(useSystemTheme = useSystem) }
    }

    // ── Profile editing ──────────────────────────────────────────────────────
    fun openEditProfile() {
        val user = _uiState.value.currentUser ?: return
        _uiState.update {
            it.copy(
                isEditingProfile = true,
                profileNameInput = user.name,
                profilePhoneInput = user.phoneNumber ?: "",
                profileRoomInput = user.roomNo ?: "",
                profileSaveError = null
            )
        }
    }

    fun closeEditProfile() {
        _uiState.update { it.copy(isEditingProfile = false, profileSaveError = null) }
    }

    fun onProfileNameChange(name: String) = _uiState.update { it.copy(profileNameInput = name) }
    fun onProfilePhoneChange(phone: String) = _uiState.update { it.copy(profilePhoneInput = phone) }
    fun onProfileRoomChange(room: String) = _uiState.update { it.copy(profileRoomInput = room) }

    fun saveProfile() {
        val state = _uiState.value
        val user = state.currentUser ?: return
        val name = state.profileNameInput.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(profileSaveError = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, profileSaveError = null) }

            val updatedUser = user.copy(
                name = name,
                phoneNumber = state.profilePhoneInput.trim().ifBlank { null },
                roomNo = state.profileRoomInput.trim().ifBlank { null }
            )

            when (val result = userRepository.saveUser(updatedUser)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSavingProfile = false, isEditingProfile = false) }
                    _events.send(SettingsEvent.ProfileSaved)
                    _events.send(SettingsEvent.ShowSnackbar("Profile updated successfully"))
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSavingProfile = false, profileSaveError = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
