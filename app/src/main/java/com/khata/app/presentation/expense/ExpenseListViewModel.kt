package com.khata.app.presentation.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.Expense
import com.khata.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            expenseRepository.observeExpensesByGroup(groupId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Sort by date descending
                        val sortedExpenses = result.data.sortedByDescending { it.date }
                        _uiState.update { it.copy(expenses = sortedExpenses, isLoading = false, error = null) }
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
