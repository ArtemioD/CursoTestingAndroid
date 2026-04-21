package com.artemiod.cursotestingandroid.cart.presentation

sealed interface CartEvent {
    data class ShowMessage(val message: String) : CartEvent
}