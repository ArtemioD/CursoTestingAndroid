package com.artemiod.cursotestingandroid.detail.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemiod.cursotestingandroid.productlist.domain.model.Product

@Composable
fun AddToCartButton(modifier: Modifier = Modifier, product: Product?, isLoading: Boolean, addToCart: () -> Unit) {

    product?.let {
        if (it.stock > 0) {
            AddToCartButtonWithStock(
                modifier = modifier,
                product = product,
                isLoading = isLoading,
                addToCart = addToCart
            )
        } else {
            AddToCartButtonNoStock(modifier = modifier)
        }
    }

}