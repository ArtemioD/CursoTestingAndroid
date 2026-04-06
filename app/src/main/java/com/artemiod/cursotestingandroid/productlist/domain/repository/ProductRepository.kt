package com.artemiod.cursotestingandroid.productlist.domain.repository

import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    // ⚠️ Set no permite valores duplicados
    fun getProductsByIds(ids: Set<String>): Flow<List<Product>>
    suspend fun refreshProducts()
}