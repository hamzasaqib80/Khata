package com.khata.app.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khata.app.core.navigation.Screen
import com.khata.app.core.theme.PrimaryTeal
import com.khata.app.core.utils.CurrencyFormatter
import com.khata.app.domain.model.Expense
import com.khata.app.domain.model.Group
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGroupDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Khata",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        uiState.currentUser?.let {
                            Text(
                                text = "Welcome, ${it.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        TextButton(
                            onClick = { showGroupDropdown = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(uiState.selectedGroup?.name ?: "Select Group")
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(
                            expanded = showGroupDropdown,
                            onDismissRequest = { showGroupDropdown = false }
                        ) {
                            uiState.groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        viewModel.selectGroup(group)
                                        showGroupDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            uiState.selectedGroup?.let { group ->
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddExpense.createRoute(group.id)) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { /* viewModel.refresh() */ },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.groups.isEmpty()) {
                DashboardLoadingSkeleton(PaddingValues(0.dp))
            } else if (uiState.groups.isEmpty()) {
                DashboardEmptyState(
                    onCreateGroup = { navController.navigate(Screen.CreateGroup.route) },
                    paddingValues = PaddingValues(0.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 80.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Balance Summary Card
                    item(key = "balance_card") {
                        BalanceSummaryCard(
                            netBalance = uiState.netBalance,
                            youOwe = uiState.youOwe,
                            youAreOwed = uiState.youAreOwed,
                            currency = uiState.selectedGroup?.currency,
                            onSettleClick = {
                                uiState.selectedGroup?.let {
                                    navController.navigate(Screen.Settlement.createRoute(it.id))
                                }
                            }
                        )
                    }

                    // Quick Actions
                    item(key = "quick_actions") {
                        uiState.selectedGroup?.let { group ->
                            QuickActionsRow(
                                onExpensesClick = { navController.navigate(Screen.ExpenseList.createRoute(group.id)) },
                                onMealsClick = { navController.navigate(Screen.MealTracker.createRoute(group.id)) },
                                onMembersClick = { navController.navigate(Screen.GroupDetail.createRoute(group.id)) }
                            )
                        }
                    }

                    // Recent Expenses Section
                    if (uiState.recentExpenses.isNotEmpty()) {
                        item(key = "recent_header") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recent Expenses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                uiState.selectedGroup?.let { group ->
                                    TextButton(onClick = {
                                        navController.navigate(Screen.ExpenseList.createRoute(group.id))
                                    }) {
                                        Text("See All")
                                    }
                                }
                            }
                        }

                        items(
                            items = uiState.recentExpenses,
                            key = { it.id }
                        ) { expense ->
                            RecentExpenseItem(
                                expense = expense,
                                currency = uiState.selectedGroup?.currency,
                                onClick = {
                                    navController.navigate(Screen.ExpenseList.createRoute(expense.groupId))
                                }
                            )
                        }
                    } else if (!uiState.isLoading) {
                        item(key = "no_expenses") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📋", fontSize = 48.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "No expenses yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Tap + to add the first expense",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceSummaryCard(
    netBalance: BigDecimal,
    youOwe: BigDecimal,
    youAreOwed: BigDecimal,
    currency: com.khata.app.domain.model.Currency?,
    onSettleClick: () -> Unit
) {
    val isPositive = netBalance >= BigDecimal.ZERO
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Your Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (currency != null)
                        CurrencyFormatter.format(netBalance.abs(), currency)
                    else netBalance.toPlainString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Color(0xFF81F5D0) else Color(0xFFFFB4AB)
                )
                Text(
                    text = if (isPositive) "You are owed" else "You owe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("You paid", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
                        Text(
                            text = if (currency != null) CurrencyFormatter.formatCompact(youAreOwed, currency) else "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("You owe", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
                        Text(
                            text = if (currency != null) CurrencyFormatter.formatCompact(youOwe, currency) else "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                if (netBalance != BigDecimal.ZERO) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onSettleClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(
                                MaterialTheme.colorScheme.onPrimary,
                                MaterialTheme.colorScheme.onPrimary
                            ))
                        )
                    ) {
                        Text("Settle Up")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onExpensesClick: () -> Unit,
    onMealsClick: () -> Unit,
    onMembersClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionItem("💸", "Expenses", onExpensesClick, Modifier.weight(1f))
        QuickActionItem("🍽️", "Meals", onMealsClick, Modifier.weight(1f))
        QuickActionItem("👥", "Members", onMembersClick, Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionItem(emoji: String, label: String, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RecentExpenseItem(
    expense: Expense,
    currency: com.khata.app.domain.model.Currency?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expense.category.name.first().toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Paid by ${expense.payer.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (currency != null) CurrencyFormatter.format(expense.amount, currency) else expense.amount.toPlainString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DashboardLoadingSkeleton(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (it == 0) 180.dp else 72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun DashboardEmptyState(onCreateGroup: () -> Unit, paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏠", fontSize = 80.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                "No Groups Yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Create a group to start splitting expenses with your roommates.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onCreateGroup,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create a Group")
            }
        }
    }
}
