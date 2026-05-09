package com.artemiod.cursotestingandroid.core.builder

import com.artemiod.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.artemiod.cursotestingandroid.cart.domain.model.CartItem

class CartItemEntityBuilder {
    private var productId: String = "product-1"
    private var quantity: Int = 1

    fun withId(id: String) = apply { this.productId = id }
    fun withQuantity(quantity: Int) = apply { this.quantity = quantity }

    fun build() = CartItemEntity(
        productId = productId,
        quantity = quantity
    )
}

fun cartItemEntity(block: CartItemEntityBuilder.() -> Unit = {}) =
    CartItemEntityBuilder().apply(block).build()