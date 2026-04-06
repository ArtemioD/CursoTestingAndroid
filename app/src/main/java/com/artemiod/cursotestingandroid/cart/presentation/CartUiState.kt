package com.artemiod.cursotestingandroid.cart.presentation

import com.artemiod.cursotestingandroid.cart.domain.model.CartSummary
import com.artemiod.cursotestingandroid.cart.presentation.model.CartItemWithPromotion

sealed class CartUiState {
    data class Success(
        val summary: CartSummary? = null,
        val cartItems: List<CartItemWithPromotion>,
        val isLoading: Boolean // para actualizar la UI,
    ) : CartUiState()

    data class Error(val message: String) : CartUiState()

    data object Loading : CartUiState() // solo para el inicio
}