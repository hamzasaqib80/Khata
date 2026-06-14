package com.khata.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ProfileSaved -> { /* already shown via snackbar */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── My Profile section ───────────────────────────────────────────
            SettingsSectionHeader("My Profile")
            uiState.currentUser?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circle with initial
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!user.roomNo.isNullOrBlank()) {
                                Text(
                                    "Room ${user.roomNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (!user.phoneNumber.isNullOrBlank()) {
                                Text(
                                    user.phoneNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = viewModel::openEditProfile) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } ?: run {
                // No user yet — shouldn't normally happen post-onboarding
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "No profile found",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Security section ─────────────────────────────────────────────
            SettingsSectionHeader("Security")
            SettingsToggleItem(
                title = "Biometric Authentication",
                subtitle = "Use fingerprint or face unlock to open Khata",
                icon = Icons.Default.Lock,
                checked = uiState.isBiometricEnabled,
                onCheckedChange = viewModel::toggleBiometric
            )

            Spacer(Modifier.height(8.dp))

            // ── Appearance section ───────────────────────────────────────────
            SettingsSectionHeader("Appearance")
            SettingsToggleItem(
                title = "Follow System Theme",
                subtitle = "Automatically switch between light and dark mode",
                icon = Icons.Default.Settings,
                checked = uiState.useSystemTheme,
                onCheckedChange = viewModel::toggleSystemTheme
            )
            if (!uiState.useSystemTheme) {
                SettingsToggleItem(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    icon = Icons.Default.Info,
                    checked = uiState.isDarkModeEnabled,
                    onCheckedChange = viewModel::toggleDarkMode
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── About section ────────────────────────────────────────────────
            SettingsSectionHeader("About")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📒", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Khata", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "کھاتہ – Hostel Expense Manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "v${uiState.appVersion}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Privacy Policy") },
                leadingContent = { Icon(Icons.Default.Info, null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) }
            )
            ListItem(
                headlineContent = { Text("Open Source Licenses") },
                leadingContent = { Icon(Icons.Default.Build, null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) }
            )
        }
    }

    // ── Edit Profile Dialog ──────────────────────────────────────────────────
    if (uiState.isEditingProfile) {
        AlertDialog(
            onDismissRequest = viewModel::closeEditProfile,
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Name
                    OutlinedTextField(
                        value = uiState.profileNameInput,
                        onValueChange = viewModel::onProfileNameChange,
                        label = { Text("Your Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = uiState.profileSaveError != null &&
                                uiState.profileNameInput.isBlank(),
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )

                    // Phone number
                    OutlinedTextField(
                        value = uiState.profilePhoneInput,
                        onValueChange = viewModel::onProfilePhoneChange,
                        label = { Text("Phone Number (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )

                    // Room number
                    OutlinedTextField(
                        value = uiState.profileRoomInput,
                        onValueChange = viewModel::onProfileRoomChange,
                        label = { Text("Room No (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Home, null) }
                    )

                    // Inline error
                    uiState.profileSaveError?.let { err ->
                        Text(
                            err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveProfile,
                    enabled = !uiState.isSavingProfile,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (uiState.isSavingProfile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeEditProfile) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
