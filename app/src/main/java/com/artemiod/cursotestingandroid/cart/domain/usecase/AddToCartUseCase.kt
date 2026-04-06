package com.artemiod.cursotestingandroid.cart.domain.usecase

import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(productId: String, quantity: Int = 1) {
        if (quantity <= 0) {
            throw AppError.Validation.QuantityMustBePositive
        }

        val product: Product =
            productRepository.getProductById(productId).first() ?: throw AppError.NotFoundError

        val existingItem = cartItemRepository.getCartItemById(productId)
        val newQuantity = (existingItem?.quantity ?: 0) + quantity

        if (newQuantity > product.stock) throw AppError.Validation.InsufficientStock(product.stock)

        cartItemRepository.addToCart(productId, quantity)
    }
}
