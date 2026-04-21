package com.artemiod.cursotestingandroid.cart.presentation

import androidx.lifecycle.ViewModel
import com.artemiod.cursotestingandroid.cart.domain.model.CartItem
import com.artemiod.cursotestingandroid.cart.domain.model.CartSummary
import com.artemiod.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ContractCart {
    val uiState: StateFlow<CartUiState>
    val event: SharedFlow<CartEvent>
    fun updateCartItem(productId: String, quantity: Int)
    fun removeFromCart(productId: String)
    fun increaseQuantity(productId: String, currentQuantity: Int)
    fun decreaseQuantity(productId: String, currentQuantity: Int)
    fun refresh()

}

class FakeCartViewModel : ViewModel(), ContractCart {
    override val uiState: StateFlow<CartUiState> = MutableStateFlow(
        CartUiState.Success(
            summary = CartSummary(subtotal = 20.0, discountTotal = 10.0, finalTotal = 10.0),
            cartItems = cartItem(),
            isLoading = false
        )
    )
    override val event: SharedFlow<CartEvent> = MutableSharedFlow()
    override fun updateCartItem(productId: String, quantity: Int) {}
    override fun removeFromCart(productId: String) {}
    override fun increaseQuantity(productId: String, currentQuantity: Int) {}
    override fun decreaseQuantity(productId: String, currentQuantity: Int) {}
    override fun refresh() {}
}

fun cartItem() = listOf(
    CartItemWithPromotion(
        cartItem = CartItem("1", 2),
        item = ProductWithPromotion(
            product = Product(
                id = "1",
                name = "Manzana",
                description = "Manzana verde",
                price = 1.5,
                category = "Frutas",
                stock = 10,
                imageUrl = ""
            ),
            promotion = null
        )
    ),
    CartItemWithPromotion(
        cartItem = CartItem("2", 1),
        item = ProductWithPromotion(
            product = Product(
                id = "2",
                name = "Banana",
                description = "Banana madura",
                price = 1.2,
                category = "Frutas",
                stock = 8,
                imageUrl = ""
            ),
            promotion = ProductPromotion.BuyXPayY(
                buy = 2,
                pay = 1,
                label = "2 por 1"
            )
        )
    ),
    CartItemWithPromotion(
        cartItem = CartItem("3", 1),
        item = ProductWithPromotion(
            product = Product(
                id = "3",
                name = "Leche",
                description = "Leche entera 1L",
                price = 2.0,
                category = "Lácteos",
                stock = 5,
                imageUrl = ""
            ),
            promotion = ProductPromotion.Percent(
                percent = 10.0,
                discountedPrice = 1.8
            )
        )
    )
)
