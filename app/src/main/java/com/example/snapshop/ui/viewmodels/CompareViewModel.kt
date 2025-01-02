package com.example.snapshop.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.model.CompareItem
import com.example.snapshop.data.model.Rating
import com.example.snapshop.ui.screens.ComparisonCriteria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CompareViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    private val _compareItems = MutableStateFlow<List<CompareItem>>(emptyList())
    val compareItems: StateFlow<List<CompareItem>> = _compareItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedCriteria = MutableStateFlow(ComparisonCriteria.BEST_VALUE)
    val selectedCriteria: StateFlow<ComparisonCriteria> = _selectedCriteria

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories

    init {
        loadCompareItems()
    }

    private fun loadCompareItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val compareDoc = db.collection("comparisons").document(userId).get().await()
                val items = compareDoc.get("items") as? List<Map<String, Any>> ?: emptyList()
                _compareItems.value = items.map { item ->
                    CompareItem(
                        productId = (item["productId"] as Number).toInt(),
                        title = item["title"] as String,
                        price = (item["price"] as Number).toDouble(),
                        description = item["description"] as String,
                        category = item["category"] as String,
                        image = item["image"] as String,
                        rating = Rating(
                            rate = (item["rating"] as Map<*, *>)["rate"] as Double,
                            count = ((item["rating"] as Map<*, *>)["count"] as Number).toInt()
                        )
                    )
                }

                // Extract unique categories from the comparison items
                _availableCategories.value = _compareItems.value.map { it.category }.distinct()

                // Automatically select the first category
                if (_availableCategories.value.isNotEmpty()) {
                    _selectedCategory.value = _availableCategories.value.first()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromComparison(productId: Int) {
        viewModelScope.launch {
            try {
                _compareItems.value = _compareItems.value.filter { it.productId != productId }
                updateComparisonInFirestore()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    private fun updateComparisonInFirestore() {
        viewModelScope.launch {
            try {
                val compareData = hashMapOf(
                    "userId" to userId,
                    "items" to _compareItems.value.map {
                        hashMapOf(
                            "productId" to it.productId,
                            "title" to it.title,
                            "price" to it.price,
                            "description" to it.description,
                            "category" to it.category,
                            "image" to it.image,
                            "rating" to mapOf(
                                "rate" to it.rating.rate,
                                "count" to it.rating.count
                            )
                        )
                    }
                )
                db.collection("comparisons").document(userId).set(compareData)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setBestProductCriteria(criteria: ComparisonCriteria) {
        _selectedCriteria.value = criteria
    }

    fun getBestProduct(): CompareItem? {
        val items = _compareItems.value.filter { it.category == _selectedCategory.value }
        if (items.isEmpty()) return null

        return when (_selectedCriteria.value) {
            ComparisonCriteria.BEST_PRICE -> items.minByOrNull { it.price }
            ComparisonCriteria.BEST_RATING -> items.maxByOrNull { it.rating.rate }
            ComparisonCriteria.BEST_VALUE -> items.maxByOrNull { it.rating.rate / it.price }
        }
    }

    fun getFilteredItems(): List<CompareItem> {
        return if (_selectedCategory.value != null) {
            _compareItems.value.filter { it.category == _selectedCategory.value }
        } else {
            _compareItems.value
        }
    }
}