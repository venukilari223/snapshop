package com.example.snapshop.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.api.RetrofitClient
import com.example.snapshop.data.model.CartItem
import com.example.snapshop.data.model.CompareItem
import com.example.snapshop.data.model.Product
import com.example.snapshop.ui.screens.SortOrder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class HomeScreenViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val api = RetrofitClient.fakeStoreApi

    private val _addToCartStatus = MutableStateFlow<String?>(null)
    val addToCartStatus: StateFlow<String?> = _addToCartStatus

    private val _addToCompareStatus = MutableStateFlow<String?>(null)
    val addToCompareStatus: StateFlow<String?> = _addToCompareStatus

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _sortOrder = MutableStateFlow(SortOrder.NONE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount

    init {
        checkUserStatus()
        fetchProducts()
        fetchCategories()
        observeCartCount()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val products = if (_selectedCategory.value != null) {
                    api.getProductsByCategory(_selectedCategory.value!!)
                } else {
                    api.getAllProducts()
                }

                // Apply search filter
                val filteredProducts = products.filter { product ->
                    product.title.contains(_searchQuery.value, ignoreCase = true) ||
                            product.description.contains(_searchQuery.value, ignoreCase = true)
                }

                // Apply sorting
                val sortedProducts = when (_sortOrder.value) {
                    SortOrder.PRICE_LOW_TO_HIGH -> filteredProducts.sortedBy { it.price }
                    SortOrder.PRICE_HIGH_TO_LOW -> filteredProducts.sortedByDescending { it.price }
                    SortOrder.RATING -> filteredProducts.sortedByDescending { it.rating.rate }
                    SortOrder.NONE -> filteredProducts
                }

                _products.value = sortedProducts
            } catch (e: Exception) {
                _error.value = "Failed to load products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                _categories.value = api.getCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun checkUserStatus() {
        _isLoading.value = true // Set loading state when checking status
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val isNewUser = document.getBoolean("isNewUser") ?: true
                _isNewUser.value = isNewUser
                _isLoading.value = false // Update loading state after fetching user status
            }
            .addOnFailureListener {
                _isNewUser.value = true // Default to true if fetching fails
                _isLoading.value = false // Stop loading state on failure
            }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            try {
                val cartItem = CartItem(
                    productId = product.id,
                    title = product.title,
                    price = product.price,
                    image = product.image
                )

                // Get current cart items
                val cartDoc = db.collection("carts").document(userId).get().await()
                val currentItems = (cartDoc.get("items") as? List<Map<String, Any>> ?: emptyList()).map {
                    CartItem(
                        productId = (it["productId"] as Number).toInt(),
                        title = it["title"] as String,
                        price = (it["price"] as Number).toDouble(),
                        image = it["image"] as String,
                        quantity = (it["quantity"] as Number).toInt()
                    )
                }.toMutableList()

                // Check if item already exists in cart
                val existingItemIndex = currentItems.indexOfFirst { it.productId == product.id }
                if (existingItemIndex != -1) {
                    // Update quantity if item exists
                    currentItems[existingItemIndex] = currentItems[existingItemIndex].copy(
                        quantity = currentItems[existingItemIndex].quantity + 1
                    )
                } else {
                    // Add new item if it doesn't exist
                    currentItems.add(cartItem)
                }

                // Update Firestore
                val cartData = hashMapOf(
                    "userId" to userId,
                    "items" to currentItems.map {
                        hashMapOf(
                            "productId" to it.productId,
                            "title" to it.title,
                            "price" to it.price,
                            "image" to it.image,
                            "quantity" to it.quantity
                        )
                    }
                )
                db.collection("carts").document(userId).set(cartData)
                _addToCartStatus.value = "Added to cart"
                // Reset status after showing
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _addToCartStatus.value = null
                }
            } catch (e: Exception) {
                _addToCartStatus.value = "Failed to add to cart: ${e.message}"
            }
        }
    }

    fun addToCompare(product: Product) {
        viewModelScope.launch {
            try {
                // First check if item is already in comparison
                val compareDoc = db.collection("comparisons").document(userId).get().await()
                val currentItems = (compareDoc.get("items") as? List<Map<String, Any>> ?: emptyList())

                // Check if item already exists
                val exists = currentItems.any { (it["productId"] as Number).toInt() == product.id }
                if (exists) {
                    _addToCompareStatus.value = "Item already in comparison list"
                    return@launch
                }

                // Convert product to CompareItem
                val compareItem = CompareItem(
                    productId = product.id,
                    title = product.title,
                    price = product.price,
                    description = product.description,
                    category = product.category,
                    image = product.image,
                    rating = product.rating
                )

                // Add new item to comparison list
                val updatedItems = currentItems.toMutableList()
                updatedItems.add(compareItem.toMap())

                // Update Firestore
                val compareData = hashMapOf(
                    "userId" to userId,
                    "items" to updatedItems
                )
                db.collection("comparisons").document(userId).set(compareData)
                _addToCompareStatus.value = "Added to comparison"

                // Reset status after showing
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _addToCompareStatus.value = null
                }
            } catch (e: Exception) {
                _addToCompareStatus.value = "Failed to add to comparison: ${e.message}"
            }
        }
    }

    // Helper extension function to convert CompareItem to Map
    private fun CompareItem.toMap(): Map<String, Any> {
        return hashMapOf(
            "productId" to productId,
            "title" to title,
            "price" to price,
            "description" to description,
            "category" to category,
            "image" to image,
            "rating" to mapOf(
                "rate" to rating.rate,
                "count" to rating.count
            )
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
        fetchProducts()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    private fun observeCartCount() {
        db.collection("carts").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.get("items") as? List<*> ?: emptyList<Any>()
                _cartItemCount.value = items.size
            }
    }
}