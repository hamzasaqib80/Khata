package com.khata.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.security.SecurePreferences
import com.khata.app.domain.model.User
import com.khata.app.domain.repository.GroupRepository
import com.khata.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.Currency
import com.khata.app.domain.model.Group
import javax.inject.Inject

data class OnboardingUiState(
    val userName: String = "",
    val phoneNumber: String = "",
    val selectedAvatarColor: String = "#1B6B5C",
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        val AVATAR_COLORS = listOf(
            "#1B6B5C", "#D32F2F", "#1976D2", "#7B1FA2",
            "#F57C00", "#388E3C", "#E64A19", "#0288D1"
        )
    }

    fun isOnboardingComplete(): Boolean {
        return securePreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun onUserNameChange(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onAvatarColorSelected(color: String) {
        _uiState.update { it.copy(selectedAvatarColor = color) }
    }

    fun onPageChange(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun completeOnboarding() {
        val state = _uiState.value
        if (state.userName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = UUID.randomUUID().toString()
            val user = User(
                id = userId,
                name = state.userName,
                avatarColorHex = state.selectedAvatarColor,
                phoneNumber = state.phoneNumber.ifBlank { null },
                roomNo = null,
                isCurrentUser = true
            )
            val userResult = userRepository.saveUser(user)

            if (userResult is Result.Success) {
                val defaultGroup = Group(
                    id = UUID.randomUUID().toString(),
                    name = "My Hostel Room",
                    description = "Default group",
                    currency = Currency.PKR,
                    members = listOf(user),
                    totalExpenses = BigDecimal.ZERO
                )
                groupRepository.createGroup(defaultGroup)
                securePreferences.putBoolean(KEY_ONBOARDING_COMPLETE, true)
                _uiState.update { it.copy(isLoading = false, isComplete = true) }
            } else if (userResult is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = userResult.message) }
            }
        }
    }
}
