package com.khata.app.presentation.settlement

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khata.app.core.utils.CurrencyFormatter
import com.khata.app.domain.model.Currency
import com.khata.app.domain.model.SimplifiedTransaction
import com.khata.app.domain.model.User
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementScreen(
    navController: NavController,
    viewModel: SettlementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettlementUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is SettlementUiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settle Up", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.isAllSettled) {
                AllSettledView(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "header") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Simplified Transactions",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        "${uiState.transactions.size} payment(s) needed to settle all debts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = uiState.transactions,
                        key = { "${it.from.id}-${it.to.id}" }
                    ) { transaction ->
                        TransactionStepCard(
                            transaction = transaction,
                            currency = uiState.group?.currency ?: Currency.PKR,
                            onMarkAsPaid = { 
                                viewModel.recordSettlement(transaction) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionStepCard(
    transaction: SimplifiedTransaction,
    currency: Currency,
    onMarkAsPaid: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Cash") }
    val paymentMethods = listOf("Cash", "EasyPaisa", "JazzCash", "Bank Transfer")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // From → To row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatarChip(transaction.from)
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = CurrencyFormatter.format(transaction.amount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(8.dp))
                UserAvatarChip(transaction.to)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Payment method chips
            Text("Pay via:", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentMethods.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Show phone number for mobile payments
            if (selectedMethod in listOf("EasyPaisa", "JazzCash")) {
                transaction.to.phoneNumber?.let { phone ->
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📱", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${transaction.to.name}'s number: $phone",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onMarkAsPaid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Mark as Paid")
            }
        }
    }
}

@Composable
private fun UserAvatarChip(user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    try {
                        Color(android.graphics.Color.parseColor(user.avatarColorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AllSettledView(modifier: Modifier = Modifier) {
    var confettiActive by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (confettiActive) {
            ConfettiCanvas(Modifier.fillMaxSize())
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🎉", fontSize = 80.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                "All Settled!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Everyone is square. No pending transactions.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ConfettiCanvas(modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
        Color(0xFFF7DC6F), Color(0xFF82E0AA), Color(0xFFAB8BFF)
    )
    val particles = remember {
        List(60) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                vx = (Random.nextFloat() - 0.5f) * 0.01f,
                vy = 0.003f + Random.nextFloat() * 0.007f,
                color = colors.random(),
                size = 6f + Random.nextFloat() * 12f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "tick"
    )

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val x = (p.x + p.vx * tick * 1000) % 1.0f * size.width
            val y = (p.y + p.vy * tick * 1000) % 1.0f * size.height
            drawCircle(color = p.color, radius = p.size, center = Offset(x, y))
        }
    }
}

private data class Particle(val x: Float, val y: Float, val vx: Float, val vy: Float, val color: Color, val size: Float)
