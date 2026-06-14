package com.khata.app.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.GroupRepository
import com.khata.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

data class GroupUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val error: String? = null
)

data class CreateGroupUiState(
    val groupName: String = "",
    val description: String = "",
    val selectedCurrency: Currency = Currency.PKR,
    val members: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val _createGroupState = MutableStateFlow(CreateGroupUiState())
    val createGroupState: StateFlow<CreateGroupUiState> = _createGroupState.asStateFlow()

    init {
        loadGroups()
        loadCurrentUser()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.observeAllGroups().collect { result ->
                _uiState.update {
                    when (result) {
                        is Result.Success -> it.copy(groups = result.data, isLoading = false)
                        is Result.Error -> it.copy(error = result.message, isLoading = false)
                        is Result.Loading -> it.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { result ->
                if (result is Result.Success && result.data != null) {
                    _createGroupState.update { it.copy(members = listOf(result.data)) }
                }
            }
        }
    }

    fun onGroupNameChange(name: String) {
        _createGroupState.update { it.copy(groupName = name, isSaveEnabled = name.isNotBlank()) }
    }

    fun onDescriptionChange(description: String) {
        _createGroupState.update { it.copy(description = description) }
    }

    fun onCurrencyChange(currency: Currency) {
        _createGroupState.update { it.copy(selectedCurrency = currency) }
    }

    fun createGroup(onSuccess: () -> Unit) {
        val state = _createGroupState.value
        if (!state.isSaveEnabled) return

        viewModelScope.launch {
            _createGroupState.update { it.copy(isLoading = true) }
            val group = Group(
                id = UUID.randomUUID().toString(),
                name = state.groupName,
                description = state.description.ifBlank { null },
                currency = state.selectedCurrency,
                members = state.members,
                totalExpenses = BigDecimal.ZERO
            )
            val result = groupRepository.createGroup(group)
            
            _createGroupState.update { 
                when (result) {
                    is Result.Success -> it.copy(isLoading = false).also { onSuccess() }
                    is Result.Error -> it.copy(isLoading = false, error = result.message)
                    else -> it.copy(isLoading = false)
                }
            }
        }
    }
}
