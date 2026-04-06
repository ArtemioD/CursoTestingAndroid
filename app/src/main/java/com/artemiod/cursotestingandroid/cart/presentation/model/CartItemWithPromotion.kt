package com.artemiod.cursotestingandroid.cart.presentation.model

import com.artemiod.cursotestingandroid.cart.domain.model.CartItem
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class CartItemWithPromotion(
    val cartItem: CartItem,
    val item: ProductWithPromotion,
)