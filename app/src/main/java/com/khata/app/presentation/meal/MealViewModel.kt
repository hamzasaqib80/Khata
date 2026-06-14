package com.khata.app.presentation.meal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.GroupRepository
import com.khata.app.domain.repository.MealRepository
import com.khata.app.domain.usecase.CalculateMessBillUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class MealUiState(
    val mealPlan: MealPlan? = null,
    val mealLogs: Map<String, List<MealLog>> = emptyMap(), // userId -> list of logs
    val groupMembers: List<User> = emptyList(),
    val messBillResult: MessBillResult? = null,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val isLoading: Boolean = false,
    val groceryCostInput: String = "",
    val error: String? = null
)

@HiltViewModel
class MealViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mealRepository: MealRepository,
    private val groupRepository: GroupRepository,
    private val calculateMessBillUseCase: CalculateMessBillUseCase
) : ViewModel() {

    private val groupId: String = savedStateHandle["groupId"] ?: run {
        // Defensive fallback: should never be null with correct navigation, but
        // a process-kill restore can leave SavedStateHandle empty.
        ""
    }
    private val _uiState = MutableStateFlow(MealUiState())
    val uiState = _uiState.asStateFlow()

    private var logsJob: Job? = null
    private var planJob: Job? = null

    init {
        loadGroupMembers()
        loadMealData()
    }

    private fun loadGroupMembers() {
        viewModelScope.launch {
            val result = groupRepository.getGroupById(groupId)
            if (result is Result.Success) {
                _uiState.update { it.copy(groupMembers = result.data?.members ?: emptyList()) }
            }
        }
    }

    private fun loadMealData() {
        val state = _uiState.value
        planJob?.cancel()
        planJob = viewModelScope.launch {
            mealRepository.observeMealPlan(groupId, state.selectedMonth, state.selectedYear)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val plan = result.data
                            _uiState.update { it.copy(mealPlan = plan, isLoading = false) }
                            if (plan != null) {
                                observeLogs(plan.id)
                                _uiState.update { it.copy(groceryCostInput = plan.totalGroceryCost.toPlainString()) }
                            } else {
                                // Create default plan if it doesn't exist
                                createDefaultPlan()
                            }
                        }
                        is Result.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                        is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    }
                }
        }
    }

    private fun createDefaultPlan() {
        val state = _uiState.value
        viewModelScope.launch {
            val newPlan = MealPlan(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                month = state.selectedMonth,
                year = state.selectedYear,
                totalGroceryCost = BigDecimal.ZERO,
                breakfastWeight = BigDecimal("0.5"),
                lunchWeight = BigDecimal("1.0"),
                dinnerWeight = BigDecimal("1.0")
            )
            mealRepository.saveMealPlan(newPlan)
        }
    }

    private fun observeLogs(mealPlanId: String) {
        logsJob?.cancel()
        logsJob = viewModelScope.launch {
            mealRepository.observeMealLogs(mealPlanId).collect { result ->
                if (result is Result.Success) {
                    val logsByUser = result.data.groupBy { it.userId }
                    _uiState.update { it.copy(mealLogs = logsByUser) }
                    calculateBillInternal()
                }
            }
        }
    }

    fun onGroceryCostChange(cost: String) {
        val filtered = cost.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(groceryCostInput = filtered) }
    }

    fun updateGroceryCost() {
        val plan = _uiState.value.mealPlan ?: return
        val newCost = _uiState.value.groceryCostInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        viewModelScope.launch {
            mealRepository.saveMealPlan(plan.copy(totalGroceryCost = newCost))
        }
    }

    fun calculateBill() {
        updateGroceryCost()
        calculateBillInternal()
    }

    private fun calculateBillInternal() {
        val state = _uiState.value
        val plan = state.mealPlan ?: return
        val members = state.groupMembers
        if (members.isEmpty()) return

        val userMealCountsMap = members.associateWith { user ->
            val logs = state.mealLogs[user.id] ?: emptyList()
            UserMealCounts(
                userId = user.id,
                breakfastCount = logs.count { it.hadBreakfast },
                lunchCount = logs.count { it.hadLunch },
                dinnerCount = logs.count { it.hadDinner }
            )
        }

        val result = calculateMessBillUseCase(plan, userMealCountsMap)
        _uiState.update { it.copy(messBillResult = result) }
    }

    fun toggleMeal(userId: String, date: LocalDate, mealType: String) {
        val plan = _uiState.value.mealPlan ?: return
        val logs = _uiState.value.mealLogs[userId] ?: emptyList()
        val existingLog = logs.find { it.date == date }
        
        val newLog = if (existingLog != null) {
            when (mealType) {
                "B" -> existingLog.copy(hadBreakfast = !existingLog.hadBreakfast)
                "L" -> existingLog.copy(hadLunch = !existingLog.hadLunch)
                "D" -> existingLog.copy(hadDinner = !existingLog.hadDinner)
                else -> existingLog
            }
        } else {
            MealLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                hadBreakfast = mealType == "B",
                hadLunch = mealType == "L",
                hadDinner = mealType == "D"
            )
        }

        viewModelScope.launch {
            mealRepository.upsertMealLog(newLog, plan.id)
        }
    }

    fun goToNextMonth() {
        val currentMonth = _uiState.value.selectedMonth
        val currentYear = _uiState.value.selectedYear
        if (currentMonth == 12) {
            _uiState.update { it.copy(selectedMonth = 1, selectedYear = currentYear + 1) }
        } else {
            _uiState.update { it.copy(selectedMonth = currentMonth + 1) }
        }
        loadMealData()
    }

    fun goToPreviousMonth() {
        val currentMonth = _uiState.value.selectedMonth
        val currentYear = _uiState.value.selectedYear
        if (currentMonth == 1) {
            _uiState.update { it.copy(selectedMonth = 12, selectedYear = currentYear - 1) }
        } else {
            _uiState.update { it.copy(selectedMonth = currentMonth - 1) }
        }
        loadMealData()
    }
}
