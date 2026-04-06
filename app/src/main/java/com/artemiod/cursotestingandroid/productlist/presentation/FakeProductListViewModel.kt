package com.artemiod.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ContractProductList {
    val uiState: StateFlow<ProductListUIState>
    val events: SharedFlow<ProductListEvent>
    val filterVisible: StateFlow<Boolean>
    fun loadProducts()
    fun setCategory(category: String?)
    fun setSortOption(sortOption: SortOption)
    fun setFilterVisible(showFilters: Boolean)
}


class FakeProductListViewModel() : ViewModel(), ContractProductList {
    override val uiState: StateFlow<ProductListUIState> =
        MutableStateFlow(
            ProductListUIState.Success(
                products = productList(),
                categories = categoryList(),
                selectedCategory = null,
                sortOption = SortOption.NONE
            )
        )
    override val events: SharedFlow<ProductListEvent> = MutableSharedFlow()
    override val filterVisible: StateFlow<Boolean> = MutableStateFlow(true)
    override fun loadProducts() {}
    override fun setCategory(category: String?) {}
    override fun setSortOption(sortOption: SortOption) {}
    override fun setFilterVisible(showFilters: Boolean) {}

    private fun productList(): List<ProductWithPromotion> {
        return listOf(
            ProductWithPromotion(
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
            ),
            ProductWithPromotion(
                product = Product(
                    id = "2",
                    name = "Leche",
                    description = "Leche entera 1L",
                    price = 2.2,
                    category = "Lácteos",
                    stock = 20,
                    imageUrl = ""
                ),
                promotion = ProductPromotion.Percent(
                    percent = 15.0,
                    discountedPrice = 1.87
                )
            ),
            ProductWithPromotion(
                product = Product(
                    id = "3",
                    name = "Pan",
                    description = "Pan fresco",
                    price = 1.0,
                    category = "Panadería",
                    stock = 30,
                    imageUrl = ""
                ),
                promotion = ProductPromotion.BuyXPayY(
                    buy = 3,
                    pay = 2,
                    label = "3 por 2"
                )
            )
        )
    }

    private fun categoryList(): List<String> {
        return listOf("Frutas", "Lácteos", "Panadería", "Verduras")
    }
}
