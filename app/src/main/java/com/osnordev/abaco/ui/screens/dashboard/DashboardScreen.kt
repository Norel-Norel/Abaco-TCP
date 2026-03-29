package com.osnordev.abaco.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.domain.calculator.BudgetCheckResult
import com.osnordev.abaco.domain.calculator.BudgetStatus
import com.osnordev.abaco.ui.screens.budget.BudgetViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    showCharts: Boolean = true,
    viewModel: DashboardViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val budgetState by budgetViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector
        PeriodSelector(
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

        if (state.isEmpty) {
            EmptyDashboard()
        } else {
            // Summary cards
            SummaryCards(
                totalIncome = state.totalIncome,
                totalExpenses = state.totalExpenses,
                netIncome = state.netIncome
            )

            if (showCharts) {
                if (state.incomeByCategory.isNotEmpty() || state.expenseByCategory.isNotEmpty()) {
                    BarChartSection(
                        incomeByCategory = state.incomeByCategory,
                        expenseByCategory = state.expenseByCategory
                    )
                }
                if (state.expenseByCategory.isNotEmpty()) {
                    PieChartSection(expenseByCategory = state.expenseByCategory)
                }
            }

            // Budget indicators
            if (budgetState.results.isNotEmpty()) {
                BudgetIndicatorsSection(results = budgetState.results)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BudgetIndicatorsSection(results: List<BudgetCheckResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Presupuestos", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            results.forEach { result ->
                val progressColor = when (result.status) {
                    BudgetStatus.EXCEEDED -> MaterialTheme.colorScheme.error
                    BudgetStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                    BudgetStatus.NORMAL -> MaterialTheme.colorScheme.primary
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(result.category, style = MaterialTheme.typography.bodySmall)
                        Text("%.0f%%".format(result.percentage * 100),
                            style = MaterialTheme.typography.bodySmall, color = progressColor)
                    }
                    LinearProgressIndicator(
                        progress = { result.percentage.coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = progressColor,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun SummaryCards(
    totalIncome: Double,
    totalExpenses: Double,
    netIncome: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            label = "Ingresos",
            amount = totalIncome,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Gastos",
            amount = totalExpenses,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
    SummaryCard(
        label = "Utilidad neta",
        amount = netIncome,
        color = if (netIncome >= 0) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${"%.2f".format(amount)} CUP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun EmptyDashboard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Sin datos este mes", style = MaterialTheme.typography.titleMedium)
        Text(
            "Ve a Transacciones para registrar tus ingresos y gastos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun prevMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 1) year - 1 to 12 else year to month - 1

private fun nextMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 12) year + 1 to 1 else year to month + 1
