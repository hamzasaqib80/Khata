package com.khata.app.presentation.expense

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khata.app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCurrencySheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AddExpenseEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                is AddExpenseEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description") },
                placeholder = { Text("e.g. Groceries, Electricity Bill") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Amount + Currency Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text(
                            uiState.selectedCurrency.symbol,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                )
                OutlinedButton(
                    onClick = { showCurrencySheet = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(uiState.selectedCurrency.code)
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            // Category Chips
            Text("Category", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpenseCategory.values()) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = {
                            Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
                        },
                        leadingIcon = {
                            Text(
                                text = when (category) {
                                    ExpenseCategory.FOOD -> "🍔"
                                    ExpenseCategory.UTILITIES -> "💡"
                                    ExpenseCategory.RENT -> "🏠"
                                    ExpenseCategory.TRANSPORT -> "🚌"
                                    ExpenseCategory.GROCERIES -> "🛒"
                                    ExpenseCategory.OTHER -> "📦"
                                }
                            )
                        }
                    )
                }
            }

            // Split Type Tabs
            Text("Split Type", style = MaterialTheme.typography.labelLarge)
            val splitTypes = listOf("Equal", "Exact", "Percentage")
            val selectedSplitIndex = when (uiState.splitType) {
                SplitType.EQUAL -> 0
                SplitType.UNEQUAL -> 1 // Unequal maps to "Exact" here
                SplitType.PERCENTAGE -> 2
            }
            TabRow(selectedTabIndex = selectedSplitIndex) {
                splitTypes.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSplitIndex == index,
                        onClick = { viewModel.onSplitTypeChange(SplitType.values()[index]) },
                        text = { Text(title) }
                    )
                }
            }

            // Members section
            Text("Participants", style = MaterialTheme.typography.labelLarge)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    uiState.groupMembers.forEach { member ->
                        val isSelected = member.id in uiState.selectedMemberIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.onMemberSelectionToggled(member.id) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(member.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            
                            if (isSelected) {
                                when (uiState.splitType) {
                                    SplitType.EQUAL -> {
                                        val share = uiState.memberShares[member.id] ?: "0.0"
                                        Text(
                                            share,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    SplitType.UNEQUAL -> {
                                        OutlinedTextField(
                                            value = uiState.memberShares[member.id] ?: "",
                                            onValueChange = { viewModel.onMemberShareChange(member.id, it) },
                                            modifier = Modifier.width(100.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                        )
                                    }
                                    SplitType.PERCENTAGE -> {
                                        OutlinedTextField(
                                            value = uiState.memberPercentages[member.id] ?: "",
                                            onValueChange = { viewModel.onMemberPercentageChange(member.id, it) },
                                            modifier = Modifier.width(100.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = { Text("%") },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = viewModel::saveExpense,
                enabled = uiState.isSaveEnabled && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Expense", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showCurrencySheet) {
        ModalBottomSheet(onDismissRequest = { showCurrencySheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Currency.values().forEach { currency ->
                    ListItem(
                        headlineContent = { Text(currency.displayName) },
                        leadingContent = { Text(currency.symbol, style = MaterialTheme.typography.titleLarge) },
                        trailingContent = { Text(currency.code) },
                        modifier = Modifier.clickable {
                            // viewModel.onCurrencyChange(currency) -- Need back-compatibility later
                            showCurrencySheet = false
                        }
                    )
                    HorizontalDivider()
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
