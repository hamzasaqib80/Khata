package com.khata.app.presentation.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.Group
import com.khata.app.domain.model.User
import com.khata.app.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: Group? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadGroup()
    }

    private fun loadGroup() {
        viewModelScope.launch {
            groupRepository.observeGroupById(groupId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(group = result.data, isLoading = false, error = null) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}
