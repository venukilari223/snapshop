package com.example.snapshop.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.snapshop.data.model.Order
import com.example.snapshop.data.model.OrderStatus
import com.example.snapshop.ui.navigation.Destinations
import com.example.snapshop.ui.viewmodels.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun OrderScreen(
    navController: NavController,
    viewModel: OrderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Force reload when screen is shown
    LaunchedEffect(Unit) {
        println("Debug: OrderScreen launched")
        viewModel.loadOrders()
    }

    // Monitor orders updates
    LaunchedEffect(orders) {
        println("Debug: Orders updated, size: ${orders.size}")
    }

    Log.d("debug", "Orders: $orders")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFE35B30)
                )
            }
            Text(
                text = "My Orders",
                style = MaterialTheme.typography.headlineMedium
            )
            Box(modifier = Modifier.size(48.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE35B30))
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No orders yet")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Destinations.HOME) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE35B30))
                    ) {
                        Text("Start Shopping", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

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
                .padding(16.dp)
        ) {
            // Order ID and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order #${order.orderId.takeLast(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE35B30)
                )
                Text(
                    text = dateFormat.format(order.orderDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Items
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.image,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Qty: ${item.quantity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFC825A) // Explicit color for price
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Total Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${String.format("%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE35B30) // Explicit color for total amount
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Status
            OrderStatusChip(status = order.status)
        }
    }
}



@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PLACED -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        OrderStatus.PROCESSING -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        OrderStatus.SHIPPED -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        OrderStatus.DELIVERED -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        OrderStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
} 