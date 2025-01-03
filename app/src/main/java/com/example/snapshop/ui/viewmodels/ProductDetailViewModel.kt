package com.example.snapshop.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.api.RetrofitClient
import com.example.snapshop.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val api = RetrofitClient.fakeStoreApi
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            try {
                val products = api.getAllProducts()
                _product.value = products.find { it.id == productId }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }


} 