package com.osnordev.abaco.ui.screens.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.ui.screens.dashboard.BarChartSection
import com.osnordev.abaco.ui.screens.dashboard.DashboardViewModel
import com.osnordev.abaco.ui.screens.dashboard.PieChartSection

/**
 * Dedicated charts screen extracted from DashboardScreen.
 * Requirements: 13.1
 */
@Composable
fun ChartsScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Gráficos", style = MaterialTheme.typography.headlineMedium)

        if (state.isEmpty) {
            Text(
                "Sin datos para mostrar. Registra transacciones para ver los gráficos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
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

        Spacer(modifier = Modifier.height(8.dp))
    }
}
