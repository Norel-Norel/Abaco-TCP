package com.osnordev.abaco.ui.screens.balance

import android.content.Intent
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.domain.calculator.AccountBalance
import com.osnordev.abaco.domain.calculator.BalanceSheet
import com.osnordev.abaco.domain.export.TrialBalancePdfExporter

@Composable
fun BalanceSheetScreen(
    viewModel: BalanceSheetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Balance General", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Fecha de corte: ${state.cutoffDate}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        val sheet = state.balanceSheet
        if (sheet == null || (sheet.assets.isEmpty() && sheet.liabilities.isEmpty() && sheet.equity.isEmpty())) {
            Text(
                "No hay asientos registrados. Crea asientos contables para ver el Balance General.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        val context = LocalContext.current
        Button(
            onClick = {
                sheet.let {
                    val uri = TrialBalancePdfExporter.export(context, it)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir Balance de Comprobación"))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
            Text("  Exportar PDF")
        }

        BalanceIndicator(sheet)

        BalanceSection(title = "Activos", accounts = sheet.assets, total = sheet.totalAssets)
        BalanceSection(title = "Pasivos", accounts = sheet.liabilities, total = sheet.totalLiabilities)
        BalanceSection(title = "Patrimonio / Resultados", accounts = sheet.equity, total = sheet.totalEquity)
    }
}

@Composable
private fun BalanceIndicator(sheet: BalanceSheet) {
    val balanced = sheet.isBalanced
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (balanced) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (balanced) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (balanced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Column {
                Text(
                    if (balanced) "Balance cuadrado ✓" else "Balance no cuadrado",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Activos: %.2f | Pasivos + Patrimonio: %.2f".format(
                        sheet.totalAssets, sheet.totalLiabilities + sheet.totalEquity
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BalanceSection(
    title: String,
    accounts: List<AccountBalance>,
    total: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()

            if (accounts.isEmpty()) {
                Text("Sin movimientos", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                accounts.forEach { account ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(account.accountName, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        Text("D: %.2f".format(account.debitTotal),
                            style = MaterialTheme.typography.bodySmall)
                        Text("  C: %.2f".format(account.creditTotal),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total $title", style = MaterialTheme.typography.titleSmall)
                Text("%.2f CUP".format(total), style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
