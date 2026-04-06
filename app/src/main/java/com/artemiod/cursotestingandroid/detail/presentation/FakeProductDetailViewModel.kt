package com.artemiod.cursotestingandroid.detail.presentation

import androidx.lifecycle.ViewModel
import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


interface ContractProductDetail {
    val uiState: StateFlow<ProductDetailUiState>
    val events : SharedFlow<ProductDetailEvent>
    fun loadProduct(productId: String)
    fun addToCart()
}


class FakeProductDetailViewModel : ViewModel(), ContractProductDetail {
    override val uiState: StateFlow<ProductDetailUiState> = MutableStateFlow(
        ProductDetailUiState(
            isLoading = false,
            item = ProductWithPromotion(
                product = Product(
                    id = "1",
                    name = "Manzana",
                    description = "Manzana verde",
                    price = 1.5,
                    category = "Frutas",
                    stock = 2,
                    imageUrl = ""
                ),
                promotion = ProductPromotion.Percent(percent = 10.0, discountedPrice = 1.35)
                //promotion = ProductPromotion.BuyXPayY(buy = 2, pay = 1, label = "2 por 1")
                //promotion = null
            )
        )
    )
    override val events: MutableSharedFlow<ProductDetailEvent> = MutableSharedFlow()
    override fun loadProduct(productId: String) {}
    override fun addToCart() {}
}
