package com.artemiod.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption.*
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel(), ContractProductList {

    override val uiState: StateFlow<ProductListUIState> = combine(
        getProductsUseCase(),
        settingsRepository.selectedCategory,
        settingsRepository.sortOption
    ) { products, category, sortOption ->

        var filteredProducts = products

        if (category != null) {
            filteredProducts = filteredProducts.filter { it.product.category == category }
        }

        val sorted = when (sortOption) {
            PRICE_ASC -> filteredProducts.sortedBy { effectivePrice(it) }
            PRICE_DESC -> filteredProducts.sortedByDescending { effectivePrice(it) }
            NONE -> filteredProducts
            DISCOUNT ->
                filteredProducts.sortedWith (
                    compareByDescending<ProductWithPromotion> { effectiveDiscountPercent(it) }
                        .thenBy { it.promotion == null }
                )
        }

        val categories = products.map { it.product.category }.distinct().sorted()

        ProductListUIState.Success(
            products = sorted,
            categories = categories,
            selectedCategory = category,
            sortOption = sortOption
        ) as ProductListUIState
    }.catch { error: Throwable ->
        emit (ProductListUIState.Error(error.message.orEmpty()))
    }.stateIn(
        scope = viewModelScope,
        initialValue = ProductListUIState.Loading,
        started = WhileSubscribed(5000)
    )

    private val _events = MutableSharedFlow<ProductListEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    override val filterVisible: StateFlow<Boolean> = settingsRepository.filtersVisible.stateIn(
        scope = viewModelScope,
        initialValue = true,
        started = WhileSubscribed(5000)
    )

    override fun setCategory(category: String?) {
        viewModelScope.launch {
            settingsRepository.setSelectedCategory(category)
        }
    }

    override fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            settingsRepository.setSortOption(sortOption)
        }
    }

    override fun setFilterVisible(showFilters: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFiltersVisible(showFilters)
        }
    }

    private fun effectiveDiscountPercent(item: ProductWithPromotion): Double {
        return when (val promo = item.promotion) {
            is ProductPromotion.Percent -> promo.percent
            else -> 0.0
        }
    }

    private fun effectivePrice(item: ProductWithPromotion): Double {
        return when (val promo = item.promotion) {
            is ProductPromotion.Percent -> promo.discountedPrice
            else -> item.product.price
        }
    }
}
