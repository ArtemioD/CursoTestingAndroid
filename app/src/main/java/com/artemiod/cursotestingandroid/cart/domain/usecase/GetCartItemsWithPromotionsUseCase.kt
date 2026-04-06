package com.artemiod.cursotestingandroid.cart.domain.usecase

import com.artemiod.cursotestingandroid.cart.domain.ex.activeAt
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject

class GetCartItemsWithPromotionsUseCase @Inject constructor(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val getPromotionForProduct: GetPromotionForProduct
) {

    operator fun invoke(): Flow<List<CartItemWithPromotion>> {
        return cartItemRepository.getCartItems().flatMapLatest { cartItems ->
            val ids = cartItems.mapTo(mutableSetOf()) { it.productId }
            if (ids.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    productRepository.getProductsByIds(ids),
                    promotionRepository.getActivePromotions()
                ) { products, promotions ->
                    val now = Instant.now()
                    val activePromotions = promotions.activeAt(now)
                    val productsById = products.associateBy { it.id }
                    cartItems.mapNotNull { cartItem ->
                        val product = productsById[cartItem.productId] ?: return@mapNotNull null
                        val promotion = getPromotionForProduct(product, activePromotions)
                        val productWithPromotion = ProductWithPromotion(product, promotion)
                        CartItemWithPromotion(cartItem = cartItem, item = productWithPromotion)
                    }
                }
            }
        }
    }
}