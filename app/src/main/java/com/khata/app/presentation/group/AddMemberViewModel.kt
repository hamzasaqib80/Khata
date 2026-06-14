package com.khata.app.presentation.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.User
import com.khata.app.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddMemberUiState(
    val name: String = "",
    val roomNo: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddMemberViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val _uiState = MutableStateFlow(AddMemberUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onRoomNoChange(roomNo: String) = _uiState.update { it.copy(roomNo = roomNo) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phoneNumber = phone) }

    fun addMember() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = state.name,
                avatarColorHex = getRandomColorHex(),
                phoneNumber = state.phoneNumber.takeIf { it.isNotBlank() },
                roomNo = state.roomNo.takeIf { it.isNotBlank() },
                isCurrentUser = false
            )

            val result = groupRepository.addMemberToGroup(groupId, newUser)
            
            when (result) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    private fun getRandomColorHex(): String {
        val colors = listOf("#FF5252", "#FF4081", "#E040FB", "#7C4DFF", "#536DFE", "#448AFF", "#40C4FF", "#18FFFF", "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41", "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40")
        return colors.random()
    }
}
