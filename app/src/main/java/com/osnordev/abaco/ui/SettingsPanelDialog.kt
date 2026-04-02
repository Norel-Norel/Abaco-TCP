package com.osnordev.abaco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
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

@Composable
fun SettingsPanelDialog(
    isDarkTheme: Boolean,
    isPinEnabled: Boolean,
    isNotificationsEnabled: Boolean,
    userProfile: UserProfile,
    onThemeToggle: (Boolean) -> Unit,
    onPinToggle: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onProfileSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var showProfileEdit by remember { mutableStateOf(false) }

    if (showProfileEdit) {
        ProfileEditDialog(
            profile = userProfile,
            onDismiss = { showProfileEdit = false },
            onSave = { updated -> onProfileSave(updated); showProfileEdit = false }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // Profile header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile.name.isNotBlank()) {
                                Text(
                                    text = userProfile.name.first().uppercaseChar().toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(Icons.Filled.Person, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Column {
                            Text(
                                text = if (userProfile.name.isNotBlank()) userProfile.name else "TCP",
                                style = MaterialTheme.typography.titleSmall,
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
                        }
                    }
                    IconButton(onClick = { showProfileEdit = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar perfil",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Theme toggle
                SettingsRow(
                    icon = { Icon(if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode, null) },
                    label = "Tema oscuro"
                ) {
                    Switch(checked = isDarkTheme, onCheckedChange = onThemeToggle)
                }

                HorizontalDivider()

                // Notifications toggle
                SettingsRow(
                    icon = { Icon(if (isNotificationsEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff, null) },
                    label = "Notificaciones"
                ) {
                    Switch(checked = isNotificationsEnabled, onCheckedChange = onNotificationsToggle)
                }

                HorizontalDivider()

                // PIN toggle
                SettingsRow(
                    icon = { Icon(Icons.Filled.Lock, null) },
                    label = "PIN de acceso"
                ) {
                    Switch(checked = isPinEnabled, onCheckedChange = { onPinToggle() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    label: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        trailing()
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
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre completo") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = businessName, onValueChange = { businessName = it },
                    label = { Text("Nombre del negocio (opcional)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = licenseNumber, onValueChange = { licenseNumber = it },
                    label = { Text("Número de licencia (opcional)") }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(UserProfile(name.trim(), businessName.trim(), licenseNumber.trim())) },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
