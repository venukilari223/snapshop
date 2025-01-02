package com.example.snapshop.data.model

data class CompareItem(
    val productId: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating
) 