package com.example.snapshop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snapshop.data.model.CompareItem
import com.example.snapshop.ui.viewmodels.CompareViewModel

enum class ComparisonCriteria {
    BEST_PRICE,
    BEST_RATING,
    BEST_VALUE
}



@Composable
fun CompareScreen(
    navController: NavController,
    viewModel: CompareViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val compareItems by viewModel.compareItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCriteria by viewModel.selectedCriteria.collectAsState()
    val bestProduct = viewModel.getBestProduct()
    val filteredItems = viewModel.getFilteredItems()
    val availableCategories by viewModel.availableCategories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Compare Products",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category filter
        if (availableCategories.isNotEmpty()) {
            FilterChips(
                categories = availableCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setSelectedCategory(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Criteria Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComparisonCriteria.values().forEach { criteria ->
                FilterChip(
                    selected = criteria == selectedCriteria,
                    onClick = { viewModel.setBestProductCriteria(criteria) },
                    label = {
                        Text(
                            when (criteria) {
                                ComparisonCriteria.BEST_PRICE -> "Best Price"
                                ComparisonCriteria.BEST_RATING -> "Best Rating"
                                ComparisonCriteria.BEST_VALUE -> "Best Value"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFC825A),
                        selectedLabelColor = Color.White,
                    )
                )
            }
        }

        // LazyColumn to handle dynamic content scroll
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Recommended product section
            bestProduct?.let { product ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE35B30)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Recommended Choice",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Based on ${selectedCriteria.name.lowercase().replace('_', ' ')}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CompareItemCard(
                                item = product,
                                isBestProduct = true,
                                onRemove = { viewModel.removeFromComparison(product.productId) },
                                onProductClick = { navController.navigate("product/${product.productId}") }
                            )
                        }
                    }
                }
            }

            // Rest of the comparison items
            items(filteredItems) { item ->
                CompareItemCard(
                    item = item,
                    isBestProduct = false,
                    onRemove = { viewModel.removeFromComparison(item.productId) },
                    onProductClick = { navController.navigate("product/${item.productId}") }
                )
            }
        }
    }
}


@Composable
fun FilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFC825A),
                    selectedLabelColor = Color.White,
                )
            )
        }
    }
}

@Composable
fun CompareItemCard(
    item: CompareItem,
    isBestProduct: Boolean,
    onRemove: () -> Unit,
    onProductClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onProductClick() }
                .padding(16.dp)
        ) {
            if (isBestProduct) {
                Text(
                    text = "Best Rated",
                    color = Color(0xFFFC825A), // Explicit color for "Best Rated" label
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Product Image
                AsyncImage(
                    model = item.image,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Product Title and Remove Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = Color.Black // Explicit color for title
                        )
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Price and Rating
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE35B30) // Explicit color for price
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rating: ${item.rating.rate} (${item.rating.count} reviews)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray // Explicit color for rating
                    )
                }
            }
        }
    }
}