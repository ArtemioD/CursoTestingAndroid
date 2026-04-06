package com.artemiod.cursotestingandroid.productlist.data.local

import android.util.Log
import com.artemiod.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.artemiod.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.artemiod.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.artemiod.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.artemiod.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import com.artemiod.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val productDao: ProductDao,
    private val promotionDao: PromotionDao,
    private val cartItemDao: CartItemDao,
) {

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductById(id: String): Flow<ProductEntity?> = productDao.getProductsById(id)

    fun getProductsByIds(productsIds: Set<String>): Flow<List<ProductEntity>> {

        // ⚠️ room no soporta listados vacios
        if (productsIds.isEmpty()) return flowOf(emptyList())

        // ⚠️ tampoco soporta set
        return productDao.getProductsByIds(productsIds.toList())
    }

    fun getAllPromotions(): Flow<List<PromotionEntity>> = promotionDao.getAllPromotions()

    suspend fun saveProducts(products: List<ProductEntity>) {
        productDao.replaceAll(products)
    }

    suspend fun savePromotions(promotions: List<PromotionEntity>) {
        promotionDao.replaceAll(promotions)
    }

    fun getAllCartItems(): Flow<List<CartItemEntity>> = cartItemDao.getAllCartItems()

    suspend fun getCartItemById(productId: String): CartItemEntity? {
        return cartItemDao.getCartItemById(productId)
    }


    suspend fun updateCartItem(cartItem: CartItemEntity) : Result<Unit> {
        return try {
            cartItemDao.updateCartItem(cartItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCartItem(cartItem: CartItemEntity) : Result<Unit> {
        return try {
            cartItemDao.deleteCartItem(cartItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart() : Result<Unit> {
        return try {
            cartItemDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToCart(cartItem: CartItemEntity) : Result<Unit> {
        return try {
            cartItemDao.insertCartItem(cartItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}