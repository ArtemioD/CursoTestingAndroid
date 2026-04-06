package com.artemiod.cursotestingandroid.cart.data.mapper

import com.artemiod.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.artemiod.cursotestingandroid.cart.domain.model.CartItem

fun CartItemEntity.toDomain(): CartItem {
    return CartItem(
        productId = productId,
        quantity = quantity
    )
}

fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        productId = productId,
        quantity = quantity
    )
}