package com.example.snapshop.data.model

import java.util.Date

data class OrderItem(
    val productId: Int,
    val title: String,
    val price: Double,
    val image: String,
    val quantity: Int
)

data class Order(
    val orderId: String,
    val userId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val orderDate: Date,
    val status: OrderStatus = OrderStatus.PLACED
)

enum class OrderStatus {
    PLACED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
} 