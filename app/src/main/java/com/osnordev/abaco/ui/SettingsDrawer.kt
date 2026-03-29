package com.osnordev.abaco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.osnordev.abaco.data.repository.UserProfile

/**
 * Right-side settings drawer content with profile header.
 * Requirements: 13.2, 13.3, 13.4, 13.5
 */
@Composable
fun SettingsDrawerContent(
    isDarkTheme: Boolean,
    isPinEnabled: Boolean,
    isNotificationsEnabled: Boolean,
    userProfile: UserProfile,
    onThemeToggle: (Boolean) -> Unit,
    onPinToggle: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onProfileSave: (UserProfile) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProfileDialog by remember { mutableStateOf(false) }

    // Profile edit dialog
    if (showProfileDialog) {
        ProfileEditDialog(
            profile = userProfile,
            onDismiss = { showProfileDialog = false },
            onSave = { updated ->
                onProfileSave(updated)
                showProfileDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Profile header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile.name.isNotBlank()) {
                            Text(
                                text = userProfile.name.first().uppercaseChar().toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar perfil",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Text(
                    text = if (userProfile.name.isNotBlank()) userProfile.name else "TCP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (userProfile.businessName.isNotBlank()) {
                    Text(
                        text = userProfile.businessName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                if (userProfile.licenseNumber.isNotBlank()) {
                    Text(
                        text = "Lic. ${userProfile.licenseNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme toggle (Req 13.3, 13.4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode, contentDescription = null)
                    Text("Tema oscuro", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = isDarkTheme, onCheckedChange = onThemeToggle)
            }

            HorizontalDivider()

            // Notifications toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isNotificationsEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        contentDescription = null
                    )
                    Text("Notificaciones", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = isNotificationsEnabled, onCheckedChange = onNotificationsToggle)
            }

            HorizontalDivider()

            // PIN toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                    Text("PIN de acceso", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = isPinEnabled, onCheckedChange = { onPinToggle() })
            }

            HorizontalDivider()

            // Quick links
            TextButton(onClick = onNavigateToQr, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("QR de cobro")
            }

            TextButton(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Configuración avanzada")
            }
        }
    }
}

@Composable
private fun ProfileEditDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var businessName by remember { mutableStateOf(profile.businessName) }
    var licenseNumber by remember { mutableStateOf(profile.licenseNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nombre del negocio (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text("Número de licencia (opcional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(UserProfile(name.trim(), businessName.trim(), licenseNumber.trim())) },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
