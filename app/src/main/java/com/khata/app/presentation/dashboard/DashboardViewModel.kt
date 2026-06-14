package com.khata.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.ExpenseRepository
import com.khata.app.domain.repository.GroupRepository
import com.khata.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val netBalance: BigDecimal = BigDecimal.ZERO,
    val youOwe: BigDecimal = BigDecimal.ZERO,
    val youAreOwed: BigDecimal = BigDecimal.ZERO,
    val recentExpenses: List<Expense> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** Tracks the current reactive jobs so we can cancel before switching groups. */
    private var expensesJob: Job? = null
    private var balanceJob: Job? = null

    init {
        loadCurrentUser()
        loadGroups()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(currentUser = result.data) }
                    is Result.Error -> _uiState.update { it.copy(error = result.message) }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.observeAllGroups().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val groups = result.data
                        _uiState.update { it.copy(groups = groups, isLoading = false) }
                        // Auto-select first group only when none is selected yet
                        if (groups.isNotEmpty() && _uiState.value.selectedGroup == null) {
                            selectGroup(groups.first())
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun selectGroup(group: Group) {
        _uiState.update { it.copy(selectedGroup = group) }
        observeGroupData(group.id)
    }

    /**
     * Cancels any previous observation jobs and starts fresh reactive streams
     * for the selected group. Both expenses and balances are live DB flows,
     * so any insert/delete automatically refreshes the UI.
     */
    private fun observeGroupData(groupId: String) {
        // ── Recent Expenses (reactive) ───────────────────────────────────────
        expensesJob?.cancel()
        expensesJob = viewModelScope.launch {
            expenseRepository.observeRecentExpenses(groupId, 5).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(recentExpenses = result.data) }
                }
            }
        }

        // ── Net Balances (reactive — recalculates on every expense change) ───
        balanceJob?.cancel()
        balanceJob = viewModelScope.launch {
            expenseRepository.observeNetBalances(groupId).collect { result ->
                if (result is Result.Success) {
                    val currentUserId = _uiState.value.currentUser?.id
                    val myBalance = result.data.find { it.user.id == currentUserId }

                    _uiState.update { state ->
                        state.copy(
                            // netBalance > 0 means "others owe you"; < 0 means "you owe others"
                            netBalance = myBalance?.netBalance ?: BigDecimal.ZERO,
                            youOwe = (myBalance?.totalOwed ?: BigDecimal.ZERO),
                            youAreOwed = (myBalance?.totalPaid ?: BigDecimal.ZERO)
                        )
                    }
                }
            }
        }
    }
}
