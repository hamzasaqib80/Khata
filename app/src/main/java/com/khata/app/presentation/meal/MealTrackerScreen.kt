package com.khata.app.presentation.meal

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khata.app.core.theme.PrimaryTeal
import com.khata.app.core.utils.DateUtils.monthYearDisplay
import com.khata.app.domain.model.MealLog
import com.khata.app.domain.model.User
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTrackerScreen(
    navController: NavController,
    viewModel: MealViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mess Tracker", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp
            )
        ) {
            // Month Navigation
            item(key = "month_nav") {
                MonthNavigationHeader(
                    month = uiState.selectedMonth,
                    year = uiState.selectedYear,
                    onPrevious = viewModel::goToPreviousMonth,
                    onNext = viewModel::goToNextMonth
                )
            }

            // Calendar Heatmap
            item(key = "meal_heatmap") {
                MonthlyMealGrid(
                    month = uiState.selectedMonth,
                    year = uiState.selectedYear,
                    mealLogs = uiState.mealLogs,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bill Calculator
            item(key = "mess_bill") {
                MessBillSection(
                    groceryCost = uiState.groceryCostInput,
                    messBillResult = uiState.messBillResult,
                    onCostChange = viewModel::onGroceryCostChange,
                    onCalculate = viewModel::calculateBill
                )
            }

            // Logging Header
            item(key = "log_header") {
                Text(
                    text = "Daily Meal Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Logs - Most recent day first for better UX
            val days = getDaysInMonth(uiState.selectedMonth, uiState.selectedYear).reversed()
            
            items(items = days, key = { it.toString() }) { date ->
                DailyLogSection(
                    date = date,
                    members = uiState.groupMembers,
                    logs = uiState.mealLogs,
                    onToggle = viewModel::toggleMeal
                )
            }
        }
    }
}

@Composable
private fun MonthNavigationHeader(month: Int, year: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev")
        }
        Text(
            text = monthYearDisplay(month, year),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
        }
    }
}

@Composable
fun MonthlyMealGrid(
    month: Int,
    year: Int,
    mealLogs: Map<String, List<MealLog>>,
    modifier: Modifier = Modifier
) {
    val days = remember(month, year) { getDaysInMonth(month, year) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            val firstDayOfWeek = (days.first().dayOfWeek.value - 1) % 7
            val cells = List(firstDayOfWeek) { null } + days

            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(
                                    if (date == null) Color.Transparent
                                    else {
                                        val totalMeals = mealLogs.values.flatten()
                                            .count { log -> 
                                                log.date == date && (log.hadBreakfast || log.hadLunch || log.hadDinner)
                                            }
                                        when {
                                            totalMeals == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            totalMeals < 3 -> PrimaryTeal.copy(alpha = 0.3f)
                                            totalMeals < 6 -> PrimaryTeal.copy(alpha = 0.6f)
                                            else -> PrimaryTeal
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                    fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    if (week.size < 7) {
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyLogSection(
    date: LocalDate,
    members: List<User>,
    logs: Map<String, List<MealLog>>,
    onToggle: (String, LocalDate, String) -> Unit
) {
    var expanded by remember { mutableStateOf(date == LocalDate.now()) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (date == LocalDate.now()) "Today" else date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show summary circles if collapsed
                if (!expanded) {
                    val count = members.sumOf { user ->
                        val log = logs[user.id]?.find { it.date == date }
                        (if (log?.hadBreakfast == true) 1 else 0) + 
                        (if (log?.hadLunch == true) 1 else 0) + 
                        (if (log?.hadDinner == true) 1 else 0)
                    }
                    Badge { Text(count.toString()) }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    members.forEach { user ->
                        val log = logs[user.id]?.find { it.date == date }
                        UserMealLoggingRow(
                            user = user,
                            log = log,
                            onToggle = { type -> onToggle(user.id, date, type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMealLoggingRow(
    user: User,
    log: MealLog?,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MealToggleButton(label = "B", active = log?.hadBreakfast ?: false) { onToggle("B") }
            MealToggleButton(label = "L", active = log?.hadLunch ?: false) { onToggle("L") }
            MealToggleButton(label = "D", active = log?.hadDinner ?: false) { onToggle("D") }
        }
    }
}

@Composable
private fun MealToggleButton(label: String, active: Boolean, onClick: () -> Unit) {
    OutlinedIconToggleButton(
        checked = active,
        onCheckedChange = { onClick() },
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.outlinedIconToggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.primary,
            checkedContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MessBillSection(
    groceryCost: String,
    messBillResult: com.khata.app.domain.model.MessBillResult?,
    onCostChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Bill Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = groceryCost,
                onValueChange = onCostChange,
                label = { Text("Total Grocery Expenses (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Calculate & Breakdown")
            }

            messBillResult?.let { result ->
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Rate per unit", style = MaterialTheme.typography.labelSmall)
                        Text("Rs. ${result.costPerUnit.setScale(2, java.math.RoundingMode.HALF_UP)}", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Units", style = MaterialTheme.typography.labelSmall)
                        Text(result.totalUnits.toPlainString(), fontWeight = FontWeight.Bold)
                    }
                }
                
                TextButton(onClick = { showDetails = !showDetails }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(if (showDetails) "Hide Members" else "Show per Member Bill")
                }

                if (showDetails) {
                    result.perUserBill.forEach { (user, bill) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(user.name, style = MaterialTheme.typography.bodySmall)
                            Text("Rs. ${bill.setScale(2, java.math.RoundingMode.HALF_UP)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun getDaysInMonth(month: Int, year: Int): List<LocalDate> {
    val firstDay = LocalDate.of(year, month, 1)
    return (0 until firstDay.lengthOfMonth()).map { firstDay.plusDays(it.toLong()) }
}
