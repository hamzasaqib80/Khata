package com.khata.app.presentation.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.GroupRepository
import com.khata.app.domain.usecase.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

sealed class AddExpenseEvent {
    object NavigateBack : AddExpenseEvent()
    data class ShowSnackbar(val message: String) : AddExpenseEvent()
}

data class AddExpenseUiState(
    val description: String = "",
    val amount: String = "",
    val selectedCurrency: Currency = Currency.PKR,
    val selectedCategory: ExpenseCategory = ExpenseCategory.OTHER,
    val splitType: SplitType = SplitType.EQUAL,
    val groupMembers: List<User> = emptyList(),
    val selectedPayer: User? = null,
    val memberShares: Map<String, String> = emptyMap(),  // userId -> exact amount string
    val memberPercentages: Map<String, String> = emptyMap(), // userId -> percentage string
    val selectedMemberIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val validationError: String? = null,
    val isSaveEnabled: Boolean = false
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddExpenseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadGroupMembers()
    }

    private fun loadGroupMembers() {
        viewModelScope.launch {
            val groupResult = groupRepository.getGroupById(groupId)
            if (groupResult is Result.Success) {
                val group = groupResult.data
                val members = group?.members ?: emptyList()
                val selectedPayer = members.find { it.isCurrentUser } ?: members.firstOrNull()
                
                _uiState.update { it.copy(
                    groupMembers = members,
                    selectedMemberIds = members.map { m -> m.id }.toSet(),
                    selectedPayer = selectedPayer,
                    selectedCurrency = group?.currency ?: Currency.PKR
                ) }
                validate()
            }
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
        validate()
    }

    fun onAmountChange(amount: String) {
        // filter non-numeric
        val filtered = amount.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return // Prevent multiple decimals
        
        _uiState.update { it.copy(amount = filtered) }
        recalculateSplitsIfNecessary()
        validate()
    }

    fun onCategoryChange(category: ExpenseCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        validate()
    }

    fun onPayerSelected(user: User) {
        _uiState.update { it.copy(selectedPayer = user) }
        validate()
    }

    fun onSplitTypeChange(splitType: SplitType) {
        _uiState.update { it.copy(splitType = splitType) }
        recalculateSplitsIfNecessary()
        validate()
    }

    fun onMemberSelectionToggled(userId: String) {
        val current = _uiState.value.selectedMemberIds
        val newSelection = if (current.contains(userId)) {
            current - userId
        } else {
            current + userId
        }
        _uiState.update { it.copy(selectedMemberIds = newSelection) }
        recalculateSplitsIfNecessary()
        validate()
    }

    fun onMemberShareChange(userId: String, amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        
        val shares = _uiState.value.memberShares.toMutableMap()
        shares[userId] = filtered
        _uiState.update { it.copy(memberShares = shares) }
        validate()
    }

    fun onMemberPercentageChange(userId: String, percentage: String) {
        val filtered = percentage.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return

        val percentages = _uiState.value.memberPercentages.toMutableMap()
        percentages[userId] = filtered
        _uiState.update { it.copy(memberPercentages = percentages) }
        validate()
    }

    private fun recalculateSplitsIfNecessary() {
        val state = _uiState.value
        val amountBd = state.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (amountBd <= BigDecimal.ZERO || state.selectedMemberIds.isEmpty()) return

        when (state.splitType) {
            SplitType.EQUAL -> {
                // Not strictly necessary as UseCase handles exact division, but good for UI preview
                val share = amountBd.divide(BigDecimal(state.selectedMemberIds.size), 2, RoundingMode.HALF_UP)
                val shares = state.selectedMemberIds.associateWith { share.toString() }
                _uiState.update { it.copy(memberShares = shares) }
            }
            SplitType.PERCENTAGE -> {
                // If switching back to percentage, default evenly
                if (state.memberPercentages.isEmpty() || state.memberPercentages.keys != state.selectedMemberIds) {
                    val defaultPct = BigDecimal("100").divide(BigDecimal(state.selectedMemberIds.size), 2, RoundingMode.HALF_UP)
                    val pcts = state.selectedMemberIds.associateWith { defaultPct.toString() }
                    _uiState.update { it.copy(memberPercentages = pcts) }
                }
            }
            SplitType.UNEQUAL -> {
                 if (state.memberShares.isEmpty() || state.memberShares.keys != state.selectedMemberIds) {
                    // Try to distribute the exact amount down
                    val share = amountBd.divide(BigDecimal(state.selectedMemberIds.size), 2, RoundingMode.HALF_UP)
                    val shares = state.selectedMemberIds.associateWith { share.toString() }
                    _uiState.update { it.copy(memberShares = shares) }
                 }
            }
        }
    }

    private fun validate() {
        val state = _uiState.value
        val amountBd = state.amount.toBigDecimalOrNull()

        var error: String? = null

        if (state.description.isBlank()) error = "Description required"
        else if (amountBd == null || amountBd <= BigDecimal.ZERO) error = "Valid amount required"
        else if (state.selectedPayer == null) error = "Payer required"
        else if (state.selectedMemberIds.isEmpty()) error = "At least one participant required"
        else {
            when (state.splitType) {
                SplitType.UNEQUAL -> {
                    val totalShares = state.selectedMemberIds.sumOf { 
                        state.memberShares[it]?.toBigDecimalOrNull() ?: BigDecimal.ZERO 
                    }
                    if (totalShares.compareTo(amountBd) != 0) {
                        error = "Exact splits must sum to $amountBd (Current: $totalShares)"
                    }
                }
                SplitType.PERCENTAGE -> {
                    val totalPct = state.selectedMemberIds.sumOf { 
                        state.memberPercentages[it]?.toBigDecimalOrNull() ?: BigDecimal.ZERO 
                    }
                    if (totalPct.compareTo(BigDecimal("100")) != 0) {
                         error = "Percentages must sum to 100% (Current: $totalPct%)"
                    }
                }
                SplitType.EQUAL -> {} // Always valid if count > 0
            }
        }

        _uiState.update { it.copy(validationError = error, isSaveEnabled = error == null) }
    }

    fun saveExpense() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val totalAmount = BigDecimal(state.amount)
            
            val participants = state.selectedMemberIds.map { userId ->
                val user = state.groupMembers.find { it.id == userId }!!
                val share = when (state.splitType) {
                    SplitType.EQUAL -> totalAmount.divide(BigDecimal(state.selectedMemberIds.size), 2, RoundingMode.HALF_UP)
                    SplitType.UNEQUAL -> state.memberShares[userId]?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    SplitType.PERCENTAGE -> {
                        val pct = state.memberPercentages[userId]?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        totalAmount.multiply(pct).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
                    }
                }
                ExpenseSplit(user, share, null)
            }

            // Adjust the last person if there's a 0.01 fractional rounding deficit
            val calculatedSum = participants.sumOf { it.shareAmount }
            val difference = totalAmount.subtract(calculatedSum)
            val adjustedParticipants = if (difference.compareTo(BigDecimal.ZERO) != 0 && participants.isNotEmpty()) {
                val pList = participants.toMutableList()
                val last = pList.removeLast()
                pList.add(last.copy(shareAmount = last.shareAmount.add(difference)))
                pList
            } else {
                participants
            }

            val expense = Expense(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                payer = state.selectedPayer!!,
                description = state.description,
                amount = totalAmount,
                currency = state.selectedCurrency,
                splitType = state.splitType,
                category = state.selectedCategory,
                participants = adjustedParticipants,
                date = LocalDate.now(),
                receiptNote = null
            )

            when (val result = addExpenseUseCase(expense)) {
                is Result.Success -> {
                   _uiState.update { it.copy(isLoading = false) }
                   _events.send(AddExpenseEvent.NavigateBack)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, validationError = result.message) }
                    _events.send(AddExpenseEvent.ShowSnackbar(result.message))
                }
                is Result.Loading -> {}
            }
        }
    }
}
