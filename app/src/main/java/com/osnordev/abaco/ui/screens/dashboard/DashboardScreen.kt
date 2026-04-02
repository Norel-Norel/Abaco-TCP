package com.osnordev.abaco.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

@Composable
fun DashboardScreen(
    showCharts: Boolean = true,
    onNavigateToJournal: () -> Unit = {},
    onNavigateToJournalForm: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Período y estado ─────────────────────────────────────────────
        PeriodHeader(
            year = state.year,
            month = state.month,
            onPrevious = {
                val (y, m) = prevMonth(state.year, state.month)
                viewModel.setPeriod(y, m)
            },
            onNext = {
                val (y, m) = nextMonth(state.year, state.month)
                viewModel.setPeriod(y, m)
            }
        )

        // ── Tarjetas Activo / Pasivo / Patrimonio ────────────────────────
        state.balanceSheet?.let { bs ->
            BalanceSummaryRow(
                totalAssets = bs.totalAssets,
                totalLiabilities = bs.totalLiabilities,
                totalEquity = bs.totalEquity,
                prevAssets = state.prevBalanceSheet?.totalAssets ?: 0.0,
                prevLiabilities = state.prevBalanceSheet?.totalLiabilities ?: 0.0,
                prevEquity = state.prevBalanceSheet?.totalEquity ?: 0.0
            )
        }

        // ── Últimos asientos ─────────────────────────────────────────────
        RecentEntriesCard(
            entries = state.recentEntries,
            onViewAll = onNavigateToJournal
        )

        // ── Acciones rápidas ─────────────────────────────────────────────
        QuickActionsRow(onNewEntry = onNavigateToJournalForm)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Period header ────────────────────────────────────────────────────────────

@Composable
private fun PeriodHeader(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale("es"))
        .uppercase()

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Período: $monthName $year — ABIERTO",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("CERRAR", style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            IconButton(onClick = onNext) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }
    }
}

// ── Balance summary ──────────────────────────────────────────────────────────

@Composable
private fun BalanceSummaryRow(
    totalAssets: Double,
    totalLiabilities: Double,
    totalEquity: Double,
    prevAssets: Double,
    prevLiabilities: Double,
    prevEquity: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BalanceCard(
            label = "ACTIVO",
            amount = totalAssets,
            prev = prevAssets,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary
        )
        BalanceCard(
            label = "PASIVO",
            amount = totalLiabilities,
            prev = prevLiabilities,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.error
        )
        BalanceCard(
            label = "PATRIMONIO",
            amount = totalEquity,
            prev = prevEquity,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun BalanceCard(
    label: String,
    amount: Double,
    prev: Double,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    val change = if (prev != 0.0) ((amount - prev) / prev) * 100 else 0.0
    val isPositive = change >= 0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatAmount(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (prev != 0.0) {
                Text(
                    text = "${if (isPositive) "▲" else "▼"} ${"%.1f".format(Math.abs(change))}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPositive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── Recent entries ───────────────────────────────────────────────────────────

@Composable
private fun RecentEntriesCard(
    entries: List<RecentEntry>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ÚLTIMOS ASIENTOS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FilledTonalButton(onClick = onViewAll, modifier = Modifier.height(32.dp)) {
                    Text("Ver todos", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Text(
                    "No hay asientos registrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                entries.forEachIndexed { index, entry ->
                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    EntryRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: RecentEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.date.format(dateFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formatAmount(entry.totalAmount),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (entry.isBalanced) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Cuadrado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Desbalance",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Quick actions ────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(onNewEntry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onNewEntry,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Nuevo Asiento")
        }
        OutlinedButton(
            onClick = { /* importar */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Importar")
        }
        OutlinedButton(
            onClick = { /* exportar */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Exportar")
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatAmount(amount: Double): String {
    return if (amount >= 1_000) "${"%.0f".format(amount / 1_000)}K CUP"
    else "${"%.0f".format(amount)} CUP"
}

private fun prevMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 1) year - 1 to 12 else year to month - 1

private fun nextMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 12) year + 1 to 1 else year to month + 1
