package com.example.snapshop.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.model.Order
import com.example.snapshop.data.model.OrderItem
import com.example.snapshop.data.model.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class OrderViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("Debug: Loading orders for userId: $userId")

                val ordersRef = db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                println("Debug: Found ${ordersRef.documents.size} orders")

                val ordersList = mutableListOf<Order>()

                ordersRef.documents.forEach { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            println("Debug: Processing order ${doc.id}")

                            // Parse items
                            val items = (data["items"] as? List<*>)?.mapNotNull { item ->
                                (item as? Map<*, *>)?.let {
                                    OrderItem(
                                        productId = (it["productId"] as Number).toInt(),
                                        title = it["title"] as String,
                                        price = (it["price"] as Number).toDouble(),
                                        image = it["image"] as String,
                                        quantity = (it["quantity"] as Number).toInt()
                                    )
                                }
                            } ?: emptyList()

                            println("Debug: Parsed ${items.size} items")

                            val order = Order(
                                orderId = data["orderId"] as String,
                                userId = data["userId"] as String,
                                items = items,
                                totalAmount = (data["totalAmount"] as Number).toDouble(),
                                orderDate = (data["orderDate"] as com.google.firebase.Timestamp).toDate(),
                                status = OrderStatus.valueOf(data["status"] as String)
                            )
                            ordersList.add(order)
                            println("Debug: Added order to list")
                        }
                    } catch (e: Exception) {
                        println("Debug: Error processing order: ${e.message}")
                        e.printStackTrace()
                    }
                }

                println("Debug: Final orders list size: ${ordersList.size}")

                // Sort orders by orderDate from newest to oldest
                _orders.value = ordersList.sortedByDescending { it.orderDate }
            } catch (e: Exception) {
                println("Debug: Error loading orders: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

}
