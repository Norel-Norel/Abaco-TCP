package com.osnordev.abaco.ui.screens.taxes

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.domain.model.BracketDetail
import com.osnordev.abaco.domain.model.TaxResult
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TaxScreen(
    viewModel: TaxViewModel = hiltViewModel()
) {
    val taxResult by viewModel.taxResult.collectAsState()
    val period by viewModel.period.collectAsState()
    val (year, month) = period

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector
        PeriodSelector(
            year = year,
            month = month,
            onPrevious = {
                val (y, m) = if (month == 1) year - 1 to 12 else year to month - 1
                viewModel.setPeriod(y, m)
            },
            onNext = {
                val (y, m) = if (month == 12) year + 1 to 1 else year to month + 1
                viewModel.setPeriod(y, m)
            }
        )

        if (taxResult == null) {
            Text(
                "Calculando tributos...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TaxContent(result = taxResult!!)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PeriodSelector(
    year: Int, month: Int,
    onPrevious: () -> Unit, onNext: () -> Unit
) {
    val monthName = Month.of(month)
        .getDisplayName(TextStyle.FULL, Locale("es"))
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
            "$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun TaxContent(result: TaxResult) {
    // Base summary
    TaxSummaryCard(result)

    // CSS breakdown
    CssTaxCard(
        grossIncome = result.grossIncome,
        cssAmount = result.cssAmount,
        cssRate = if (result.grossIncome > 0) result.cssAmount / result.grossIncome else 0.0
    )

    // IIP breakdown
    IipTaxCard(
        annualNetIncome = result.netIncome,
        iipAmount = result.iipAmount,
        bracketDetails = result.iipBracketDetails
    )
}

@Composable
private fun TaxSummaryCard(result: TaxResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Resumen del período",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            TaxRow("Ingresos brutos", result.grossIncome)
            TaxRow("Gastos deducibles", result.totalExpenses)
            HorizontalDivider()
            TaxRow("Utilidad neta (anualizada)", result.netIncome, bold = true)
            HorizontalDivider()
            TaxRow("Total CSS", result.cssAmount, color = MaterialTheme.colorScheme.error)
            TaxRow("Total IIP", result.iipAmount, color = MaterialTheme.colorScheme.error)
            HorizontalDivider()
            TaxRow(
                label = "Total a pagar",
                amount = result.cssAmount + result.iipAmount,
                bold = true,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CssTaxCard(grossIncome: Double, cssAmount: Double, cssRate: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Contribución a la Seguridad Social (CSS)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TaxRow("Base de cálculo (ingresos brutos)", grossIncome)
            TaxRow("Tasa aplicada", cssRate * 100, suffix = "%")
            HorizontalDivider()
            TaxRow("CSS a pagar", cssAmount, bold = true, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun IipTaxCard(
    annualNetIncome: Double,
    iipAmount: Double,
    bracketDetails: List<BracketDetail>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Impuesto sobre Ingresos Personales (IIP)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TaxRow("Base de cálculo (utilidad neta anual)", annualNetIncome)

            if (bracketDetails.isEmpty()) {
                Text(
                    "Exento — utilidad neta dentro del primer tramo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Desglose por tramos:", style = MaterialTheme.typography.labelMedium)
                bracketDetails.forEach { detail ->
                    BracketRow(detail)
                }
            }

            HorizontalDivider()
            TaxRow("IIP a pagar", iipAmount, bold = true, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun BracketRow(detail: BracketDetail) {
    val from = detail.bracket.from.toLong()
    val to = detail.bracket.to?.toLong()
    val rangeLabel = if (to != null) "$from – $to CUP" else "Más de $from CUP"
    val rateLabel = "${"%.0f".format(detail.bracket.rate * 100)}%"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                "$rangeLabel  ·  $rateLabel",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Base en tramo: ${"%.2f".format(detail.taxableAmount)} CUP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Impuesto: ${"%.2f".format(detail.taxAmount)} CUP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TaxRow(
    label: String,
    amount: Double,
    bold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    suffix: String = " CUP"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${"%.2f".format(amount)}$suffix",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
