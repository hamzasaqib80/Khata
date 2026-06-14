package com.khata.app.presentation.settlement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.SettlementRepository
import com.khata.app.domain.usecase.SimplifyDebtsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettlementUiState(
    val isLoading: Boolean = false,
    val transactions: List<SimplifiedTransaction> = emptyList(),
    val group: Group? = null,
    val isAllSettled: Boolean = false,
    val error: String? = null
)

sealed class SettlementUiEvent {
    data class ShowSnackbar(val message: String) : SettlementUiEvent()
    data object NavigateBack : SettlementUiEvent()
}

@HiltViewModel
class SettlementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: com.khata.app.domain.repository.GroupRepository,
    private val settlementRepository: SettlementRepository,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(SettlementUiState())
    val uiState: StateFlow<SettlementUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettlementUiEvent>()
    val events: SharedFlow<SettlementUiEvent> = _events.asSharedFlow()

    init {
        loadSettlements()
    }

    private fun loadSettlements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val groupResult = groupRepository.getGroupById(groupId)
            if (groupResult is Result.Success && groupResult.data != null) {
                val group = groupResult.data
                _uiState.update { it.copy(group = group) }

                viewModelScope.launch(Dispatchers.Default) {
                    val result = simplifyDebtsUseCase(groupId, group.currency)
                    _uiState.update {
                        when (result) {
                            is Result.Success -> it.copy(
                                transactions = result.data,
                                isAllSettled = result.data.isEmpty(),
                                isLoading = false
                            )
                            is Result.Error -> it.copy(error = result.message, isLoading = false)
                            is Result.Loading -> it.copy(isLoading = true)
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Group not found") }
            }
        }
    }

    fun recordSettlement(transaction: SimplifiedTransaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = settlementRepository.recordSettlement(transaction, groupId)
            if (result is Result.Success) {
                _events.emit(SettlementUiEvent.ShowSnackbar("Payment recorded!"))
                loadSettlements() // Refresh
            } else if (result is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun refresh() {
        loadSettlements()
    }
}
