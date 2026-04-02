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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.osnordev.abaco.data.repository.UserProfile
import com.osnordev.abaco.ui.navigation.Screen

data class DrawerItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val group: DrawerGroup = DrawerGroup.MAIN
)

enum class DrawerGroup { MAIN, ACCOUNTING, MANAGEMENT, OTHER }

val drawerItems = listOf(
    // Principal
    DrawerItem(Screen.Dashboard,    "Dashboard",         Icons.Filled.Dashboard,      DrawerGroup.MAIN),
    DrawerItem(Screen.Transactions, "Transacciones",     Icons.Filled.Receipt,        DrawerGroup.MAIN),
    DrawerItem(Screen.Search,       "Búsqueda",          Icons.Filled.Search,         DrawerGroup.MAIN),

    // Contabilidad
    DrawerItem(Screen.Journal,      "Asientos Contables",Icons.Filled.MenuBook,       DrawerGroup.ACCOUNTING),
    DrawerItem(Screen.BalanceSheet, "Balance General",   Icons.Filled.AccountBalance, DrawerGroup.ACCOUNTING),
    DrawerItem(Screen.Taxes,        "Tributos ONAT",     Icons.Filled.BarChart,       DrawerGroup.ACCOUNTING),
    DrawerItem(Screen.Reports,      "Reportes",          Icons.Filled.BarChart,       DrawerGroup.ACCOUNTING),

    // Gestión
    DrawerItem(Screen.Salary,       "Nómina",            Icons.Filled.Payments,       DrawerGroup.MANAGEMENT),
    DrawerItem(Screen.Inventory,    "Inventario",        Icons.Filled.Inventory,      DrawerGroup.MANAGEMENT),
    DrawerItem(Screen.Contacts,     "Contactos",         Icons.Filled.Contacts,       DrawerGroup.MANAGEMENT),
    DrawerItem(Screen.Budgets,      "Presupuestos",      Icons.Filled.CalendarMonth,  DrawerGroup.MANAGEMENT),
    DrawerItem(Screen.PaymentDues,  "Vencimientos",      Icons.Filled.People,         DrawerGroup.MANAGEMENT),

    // Otros
    DrawerItem(Screen.QrCode,       "QR de Cobro",       Icons.Filled.QrCode,         DrawerGroup.OTHER),
    DrawerItem(Screen.Settings,     "Configuración",     Icons.Filled.Settings,       DrawerGroup.OTHER),
)

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    userProfile: UserProfile,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header con perfil ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userProfile.name.isNotBlank()) userProfile.name.first().uppercaseChar().toString() else "A",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = if (userProfile.name.isNotBlank()) userProfile.name else "Ábaco",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (userProfile.businessName.isNotBlank()) userProfile.businessName else "Sistema Contable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────

        Spacer(modifier = Modifier.height(8.dp))

        DrawerSection(label = null) {
            drawerItems.filter { it.group == DrawerGroup.MAIN }.forEach { item ->
                DrawerNavItem(item = item, selected = currentRoute == item.screen.route, onClick = { onNavigate(item.screen) })
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        DrawerSection(label = "Contabilidad") {
            drawerItems.filter { it.group == DrawerGroup.ACCOUNTING }.forEach { item ->
                DrawerNavItem(item = item, selected = currentRoute == item.screen.route, onClick = { onNavigate(item.screen) })
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        DrawerSection(label = "Gestión") {
            drawerItems.filter { it.group == DrawerGroup.MANAGEMENT }.forEach { item ->
                DrawerNavItem(item = item, selected = currentRoute == item.screen.route, onClick = { onNavigate(item.screen) })
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        DrawerSection(label = null) {
            drawerItems.filter { it.group == DrawerGroup.OTHER }.forEach { item ->
                DrawerNavItem(item = item, selected = currentRoute == item.screen.route, onClick = { onNavigate(item.screen) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerSection(label: String?, content: @Composable () -> Unit) {
    if (label != null) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 4.dp)
        )
    }
    content()
}

@Composable
private fun DrawerNavItem(
    item: DrawerItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(item.icon, contentDescription = null) },
        label = { Text(item.label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
