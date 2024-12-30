package com.example.snapshop.data.api

import com.example.snapshop.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Path

interface FakeStoreApi {
    @GET("products")
    suspend fun getAllProducts(): List<Product>

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<Product>
} 