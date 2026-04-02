package com.osnordev.abaco.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.data.local.InventoryItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onAddItem: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventario") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar producto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Buscar producto o categoría") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Tabs: Todos / Stock bajo
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Todos (${items.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Stock bajo")
                            if (lowStockItems.isNotEmpty()) {
                                Badge { Text("${lowStockItems.size}") }
                            }
                        }
                    }
                )
            }

            val displayItems = if (selectedTab == 0) items else lowStockItems

            if (displayItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Inventory else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (selectedTab == 0) "No hay productos en el inventario"
                            else "No hay productos con stock bajo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayItems, key = { it.id }) { item ->
                        InventoryItemCard(item = item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItemEntity,
    onClick: () -> Unit
) {
    val isLowStock = item.minStock > 0 && item.quantity <= item.minStock

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(item.name, style = MaterialTheme.typography.titleSmall)
                    if (isLowStock) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Stock bajo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }
                if (item.category.isNotBlank()) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.salePrice > 0) {
                    Text(
                        "Venta: ${"%.2f".format(item.salePrice)} CUP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${"%.2f".format(item.quantity)} ${item.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isLowStock) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                )
                if (item.minStock > 0) {
                    Text(
                        "Mín: ${"%.2f".format(item.minStock)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
