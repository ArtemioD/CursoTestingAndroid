package com.artemiod.cursotestingandroid.cart.domain.usecase

import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateCartItemUseCase @Inject constructor(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(productId: String, quantity: Int) {

        if (quantity < 0) throw AppError.Validation.QuantityMustBePositive

        if (quantity == 0) {
            cartItemRepository.removeFromCart(productId)
            return
        }

        val product =
            productRepository.getProductById(productId).first() ?: throw AppError.NotFoundError

        if (quantity > product.stock) {
            throw AppError.Validation.InsufficientStock(product.stock)
        }

        cartItemRepository.updateQuantity(productId, quantity)

    }
}