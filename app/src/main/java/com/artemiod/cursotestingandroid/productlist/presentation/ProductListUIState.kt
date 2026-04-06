package com.artemiod.cursotestingandroid.productlist.presentation

import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption

sealed class ProductListUIState {
    data object Loading : ProductListUIState()
    data class Error(val message: String) : ProductListUIState()
    data class Success(
        val products: List<ProductWithPromotion>,
        val categories: List<String>,
        val selectedCategory: String?,
        val sortOption: SortOption
    ) : ProductListUIState()
}