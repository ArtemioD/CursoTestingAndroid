package com.artemiod.cursotestingandroid.detail.presentation

import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class ProductDetailUiState(
    val item : ProductWithPromotion? = null,
    val isLoading: Boolean = true
)
