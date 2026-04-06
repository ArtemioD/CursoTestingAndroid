package com.artemiod.cursotestingandroid.cart.presentation

sealed interface CartEvent {
    data class showMessage(val message: String) : CartEvent
}