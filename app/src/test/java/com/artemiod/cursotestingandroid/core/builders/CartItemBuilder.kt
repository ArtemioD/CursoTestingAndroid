package com.artemiod.cursotestingandroid.core.builders

import com.artemiod.cursotestingandroid.cart.domain.model.CartItem

class CartItemBuilder {
    private var productId: String = "product-1"
    private var quantity: Int = 1

    fun withId(id: String) = apply { this.productId = id }
    fun withQuantity(quantity: Int) = apply { this.quantity = quantity }

    fun build() = CartItem(
        productId = productId,
        quantity = quantity
    )
}

fun cartItem(block: CartItemBuilder.() -> Unit = {}) = CartItemBuilder().apply(block).build()
