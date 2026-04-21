package com.artemiod.cursotestingandroid.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.artemiod.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.artemiod.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartItemRepository: CartItemRepository,
    getCartSummaryUseCase: GetCartSummaryUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    getCartItemsWithPromotionsUseCase: GetCartItemsWithPromotionsUseCase
) : ViewModel(), ContractCart {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override val uiState: StateFlow<CartUiState> = combine(
        refreshTrigger.onStart { emit(Unit) },
        getCartItemsWithPromotionsUseCase(),
        getCartSummaryUseCase()
    ) { _, cartItemsWithPromotions, summary ->
        CartUiState.Success(
            summary = summary,
            cartItems = cartItemsWithPromotions,
            isLoading = false
        ) as CartUiState
    }.catch { e ->
        CartEvent.ShowMessage(e.message.orEmpty())
        CartUiState.Error(e.message.orEmpty())
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = CartUiState.Loading
    )

    private val _event = MutableSharedFlow<CartEvent>(extraBufferCapacity = 1)
    override val event: SharedFlow<CartEvent> = _event.asSharedFlow()

    override fun updateCartItem(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                updateCartItemUseCase(productId, quantity)
            } catch (e: Exception) {
                _event.emit(CartEvent.ShowMessage(e.message.orEmpty()))
            }
        }
    }

    override fun removeFromCart(productId: String) {
        viewModelScope.launch {
            try {
                cartItemRepository.removeFromCart(productId)
            } catch (e: Exception) {
                _event.emit(CartEvent.ShowMessage(e.message.orEmpty()))
            }
        }
    }

    override fun increaseQuantity(productId: String, currentQuantity: Int) {
        updateCartItem(productId, currentQuantity + 1)
    }

    override fun decreaseQuantity(productId: String, currentQuantity: Int) {
        if (currentQuantity > 1) {
            updateCartItem(productId, currentQuantity - 1)
        } else {
            removeFromCart(productId)
        }
    }

    override fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }
}