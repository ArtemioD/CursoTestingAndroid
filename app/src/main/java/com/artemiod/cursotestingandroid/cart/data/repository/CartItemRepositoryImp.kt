package com.artemiod.cursotestingandroid.cart.data.repository

import android.util.Log
import com.artemiod.cursotestingandroid.cart.data.mapper.toDomain
import com.artemiod.cursotestingandroid.cart.data.mapper.toEntity
import com.artemiod.cursotestingandroid.cart.domain.model.CartItem
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.productlist.data.local.LocalDataSource
import com.artemiod.cursotestingandroid.productlist.data.mappers.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartItemRepositoryImp @Inject constructor(
    private val localDataSource: LocalDataSource
) : CartItemRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return localDataSource.getAllCartItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToCart(productId: String, quantity: Int) {
        val existingItem = localDataSource.getCartItemById(productId)
        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            localDataSource.updateCartItem(existingItem.copy(quantity = newQuantity))
        } else {
            localDataSource.addToCart(
                CartItem(
                    productId = productId,
                    quantity = quantity
                ).toEntity()
            )
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val item = localDataSource.getCartItemById(productId) ?: throw AppError.NotFoundError
        localDataSource.deleteCartItem(item)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        val item = localDataSource.getCartItemById(productId) ?: throw AppError.NotFoundError
        localDataSource.updateCartItem(item.copy(quantity = quantity))
    }

    override suspend fun clearCart() {
        localDataSource.clearCart()
    }

    override suspend fun getCartItemById(productId: String): CartItem? {
        return localDataSource.getCartItemById(productId)?.toDomain()
    }
}