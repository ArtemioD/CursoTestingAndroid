package com.artemiod.cursotestingandroid.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemiod.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.core.domain.model.AppError.DatabaseError
import com.artemiod.cursotestingandroid.core.domain.model.AppError.NetworkError
import com.artemiod.cursotestingandroid.core.domain.model.AppError.NotFoundError
import com.artemiod.cursotestingandroid.core.domain.model.AppError.UnknownError
import com.artemiod.cursotestingandroid.core.domain.model.AppError.Validation
import com.artemiod.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailWithPromotionUseCase: GetProductDetailWithPromotionUseCase,
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel(), ContractProductDetail {

    private val productIdFlow = MutableStateFlow<String?>(null)

    override val uiState: StateFlow<ProductDetailUiState> = productIdFlow
        .filterNotNull()
        .flatMapLatest { productId ->
            getProductDetailWithPromotionUseCase(productId)
                .map { product ->
                    ProductDetailUiState(item = product, isLoading = false)
                }
                .onStart { emit(ProductDetailUiState(isLoading = true)) }
                .catch { e: Throwable ->
                    val error = if (e is AppError) e else UnknownError(e.message)
                    handleError(error)
                    emit(ProductDetailUiState(isLoading = false))
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductDetailUiState(isLoading = true)
        )

    private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    override fun loadProduct(productId: String) {
        productIdFlow.value = productId
    }

    override fun addToCart() {
        val product = uiState.value.item?.product?.id ?: return
        viewModelScope.launch {
            try {
                addToCartUseCase(product)
                _events.emit(ProductDetailEvent.SUCCESS_ADD_TO_CART)
            } catch (e: AppError) {
                handleError(e)
            } catch (e: Exception) {
                handleError(UnknownError(e.message))
            }
        }
    }

    private suspend fun handleError(error: AppError) {
        val newEvent = when (error) {
            is UnknownError, DatabaseError, NotFoundError, Validation.QuantityMustBePositive -> ProductDetailEvent.UNKNOWN_ERROR
            NetworkError -> ProductDetailEvent.NETWORK_ERROR
            is Validation.InsufficientStock -> ProductDetailEvent.INSUFFICIENT_STOCK
        }

        _events.emit(newEvent)
    }

}