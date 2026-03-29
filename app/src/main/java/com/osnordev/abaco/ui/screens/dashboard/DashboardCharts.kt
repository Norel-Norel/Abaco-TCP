package com.osnordev.abaco.ui.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

// Palette for pie chart slices
private val sliceColors = listOf(
    Color(0xFF1B6B4A), Color(0xFF7A5900), Color(0xFF3B5FA0),
    Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF2196F3)
)

@Composable
fun BarChartSection(
    incomeByCategory: List<CategoryTotal>,
    expenseByCategory: List<CategoryTotal>
) {
    val allCategories = (incomeByCategory.map { it.category } +
            expenseByCategory.map { it.category }).distinct()

    val incomeMap = incomeByCategory.associate { it.category to it.total }
    val expenseMap = expenseByCategory.associate { it.category to it.total }

    val incomeValues = allCategories.map { incomeMap[it] ?: 0.0 }
    val expenseValues = allCategories.map { expenseMap[it] ?: 0.0 }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(incomeValues, expenseValues) {
        modelProducer.runTransaction {
            columnSeries {
                series(incomeValues.map { it.toFloat() })
                series(expenseValues.map { it.toFloat() })
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Ingresos vs Gastos por categoría",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )
        }
    }
}

@Composable
fun PieChartSection(expenseByCategory: List<CategoryTotal>) {
    val total = expenseByCategory.sumOf { it.total }
    if (total == 0.0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Distribución de gastos",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(140.dp)) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        var startAngle = -90f
                        expenseByCategory.forEachIndexed { index, item ->
                            val sweep = (item.total / total * 360f).toFloat()
                            drawArc(
                                color = sliceColors[index % sliceColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, size.height)
                            )
                            startAngle += sweep
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    expenseByCategory.forEachIndexed { index, item ->
                        val pct = item.total / total * 100
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(color = sliceColors[index % sliceColors.size])
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${item.category} (${"%.0f".format(pct)}%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
