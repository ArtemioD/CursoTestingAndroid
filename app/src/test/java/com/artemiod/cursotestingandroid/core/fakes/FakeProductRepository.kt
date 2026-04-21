package com.artemiod.cursotestingandroid.core.fakes

import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeProductRepository : ProductRepository {

    private val _products = MutableStateFlow<List<Product>>(emptyList())

    fun setProducts(products: List<Product>) {
        _products.value = products
    }

    override fun getProducts(): Flow<List<Product>> = _products.asStateFlow()

    override fun getProductById(id: String): Flow<Product?> {
        return _products.asStateFlow().map { products ->
            products.find { it.id == id }
        }
    }

    override fun getProductsByIds(ids: Set<String>): Flow<List<Product>> {
        return _products.asStateFlow().map { products ->
            products.filter { it.id in ids }
        }
    }

    override suspend fun refreshProducts() {
        // no effect
    }
}