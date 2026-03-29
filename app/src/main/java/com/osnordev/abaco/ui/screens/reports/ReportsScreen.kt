package com.osnordev.abaco.ui.screens.reports

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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import com.osnordev.abaco.domain.calculator.MonthlyFlow
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Reportes", style = MaterialTheme.typography.headlineMedium)

        // Period selector
        PeriodSelector(
            year = state.selectedYear,
            month = state.selectedMonth,
            onPrevious = {
                val ym = YearMonth.of(state.selectedYear, state.selectedMonth).minusMonths(1)
                viewModel.setPeriod(ym.year, ym.monthValue)
            },
            onNext = {
                val ym = YearMonth.of(state.selectedYear, state.selectedMonth).plusMonths(1)
                viewModel.setPeriod(ym.year, ym.monthValue)
            }
        )

        // Export CSV button (Req 14.1)
        OutlinedButton(
            onClick = {
                val intent = viewModel.exportCsv(context)
                context.startActivity(Intent.createChooser(intent, "Exportar CSV"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Text("  Exportar CSV del período")
        }

        // Export XLSX button (Req 14.2)
        OutlinedButton(
            onClick = {
                val intent = viewModel.exportXlsx(context)
                context.startActivity(Intent.createChooser(intent, "Exportar Excel"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Text("  Exportar Excel del período")
        }

        // Cash flow section
        CashFlowSection(flows = state.cashFlow)

        // Tax projection section
        state.projection?.let { proj ->
            TaxProjectionSection(projection = proj)
        }

        // Period comparison section
        state.comparison?.let { comp ->
            PeriodComparisonSection(comparison = comp)
        }

        Spacer(modifier = Modifier.height(8.dp))
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
        Text("$monthName $year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun CashFlowSection(flows: List<MonthlyFlow>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Flujo de caja (últimos 12 meses)", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            if (flows.isEmpty()) {
                Text("Sin datos disponibles", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                flows.forEach { flow ->
                    val monthLabel = flow.yearMonth.month
                        .getDisplayName(TextStyle.SHORT, Locale("es"))
                        .replaceFirstChar { it.uppercase() } + " ${flow.yearMonth.year}"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(monthLabel, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        Text(
                            "+${"%.0f".format(flow.income)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " / -${"%.0f".format(flow.expenses)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        val netColor = if (flow.net >= 0) MaterialTheme.colorScheme.tertiary
                                       else MaterialTheme.colorScheme.error
                        Text(
                            " = ${"%.0f".format(flow.net)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = netColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxProjectionSection(projection: com.osnordev.abaco.domain.calculator.TaxProjection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Proyección tributaria anual (basada en ${projection.basedOnMonths} mes(es))",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            ProjectionRow("Ingresos anualizados", projection.annualizedGrossIncome)
            ProjectionRow("Utilidad neta anualizada", projection.annualizedNetIncome)
            ProjectionRow("CSS estimada", projection.estimatedCss)
            ProjectionRow("IIP estimado", projection.estimatedIip)
            ProjectionRow("Total tributos estimados", projection.totalEstimatedTax,
                bold = true, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ProjectionRow(
    label: String,
    amount: Double,
    bold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            "${"%.2f".format(amount)} CUP",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun PeriodComparisonSection(comparison: PeriodComparison) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Comparativa con mes anterior", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            ComparisonRow("Ingresos", comparison.currentIncome, comparison.previousIncome, comparison.incomeChange)
            ComparisonRow("Gastos", comparison.currentExpenses, comparison.previousExpenses, comparison.expensesChange)
            ComparisonRow("Utilidad neta", comparison.currentNet, comparison.previousNet, comparison.netChange)
        }
    }
}

@Composable
private fun ComparisonRow(label: String, current: Double, previous: Double, change: Double) {
    val changeColor = when {
        change > 0 -> MaterialTheme.colorScheme.primary
        change < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val changeSign = if (change >= 0) "+" else ""
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
        Text("${"%.0f".format(current)}", style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f))
        Text(
            "$changeSign${"%.1f".format(change * 100)}%",
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
