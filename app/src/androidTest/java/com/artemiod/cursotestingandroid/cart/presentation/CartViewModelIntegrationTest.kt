package com.artemiod.cursotestingandroid.cart.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.artemiod.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.artemiod.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatchers
import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.artemiod.cursotestingandroid.core.utils.asAssets
import com.artemiod.cursotestingandroid.core.utils.awaitStateMatching
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartViewModelIntegrationTest {

    private companion object {
        const val PRODUCT_ID = "p1"
        const val UPDATED_QUANTITY = 2
        const val INITIAL_QUANTITY = 1
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var cartItemRepository: CartItemRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Inject
    lateinit var getCarSummaryUseCase: GetCartSummaryUseCase

    @Inject
    lateinit var updateCartItemUseCase: UpdateCartItemUseCase

    @Inject
    lateinit var getCartItemsWithPromotionsUseCase: GetCartItemsWithPromotionsUseCase

    @Before
    fun setUp() = runTest {
        mockWebServer.server.dispatcher = MiniMarketApiDispatchers(
            productJson = "product_list_default.json".asAssets()
        )
        hilt.inject()
        cartItemRepository.clearCart()

        productRepository.refreshProducts()
        promotionRepository.refreshPromotions()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenCartWithItems_whenViewModelCollectsUIState_thenSuccessWithSummary() = runTest {
        cartItemRepository.addToCart(PRODUCT_ID, UPDATED_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {

            // usando el awaitSuccessMatching
            val result = awaitSuccessMatching {
                it.summary != null && it.cartItems.isNotEmpty()
            }

            assertTrue(result.cartItems.isNotEmpty())
            assertTrue(result.summary != null)
            assertEquals(20.0, result.summary!!.subtotal, 0.01)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun givenCartWithItems_whenViewModelCollectsUIState_thenSuccessWithSummary_V2() = runTest {
        cartItemRepository.addToCart(PRODUCT_ID, UPDATED_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {

            // lo mismo que el awaitSuccessMatching, solo que es generico con <T>
            val result = awaitStateMatching { state ->
                // aca podemos cambiar y testiar deferentes estados en otro no
                state is CartUiState.Success &&
                        state.summary != null && state.cartItems.isNotEmpty()
            }
            val success = result as CartUiState.Success

            assertTrue(success.cartItems.isNotEmpty())
            assertTrue(success.summary != null)
            assertEquals(20.0, result.summary!!.subtotal, 0.01)

            cancelAndConsumeRemainingEvents()
        }
    }


    @Test
    fun givenSingleProduct_whenIncreaseQuantity_thenQuantityIsUpdated() = runTest {
        cartItemRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {

            val success = awaitSuccessMatching { state ->
                state.cartItems.any {
                    it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == INITIAL_QUANTITY
                }
            }
            assertEquals(INITIAL_QUANTITY, success.cartItems.first().cartItem.quantity)


            viewModel.increaseQuantity(PRODUCT_ID, INITIAL_QUANTITY)


            val updateSuccess = awaitSuccessMatching { state ->
                state.cartItems.any {
                    it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == UPDATED_QUANTITY
                }
            }
            assertEquals(UPDATED_QUANTITY, updateSuccess.cartItems.first().cartItem.quantity)


            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun givenSingleProduct_whenDecreaseToZero_thenCartBecomesEmpty() = runTest {
        cartItemRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {

            val success = awaitSuccessMatching { state ->
                state.cartItems.any {
                    it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == INITIAL_QUANTITY
                }
            }
            assertEquals(INITIAL_QUANTITY, success.cartItems.first().cartItem.quantity)

            viewModel.decreaseQuantity(PRODUCT_ID, INITIAL_QUANTITY)

            val emptySuccess = awaitSuccessMatching { state ->
                state.cartItems.isEmpty()
            }
            assertTrue(emptySuccess.cartItems.isEmpty())

            cancelAndConsumeRemainingEvents()
        }
    }

    private fun createViewModel() = CartViewModel(
        cartItemRepository = cartItemRepository,
        getCartSummaryUseCase = getCarSummaryUseCase,
        updateCartItemUseCase = updateCartItemUseCase,
        getCartItemsWithPromotionsUseCase = getCartItemsWithPromotionsUseCase
    )

    private suspend fun ReceiveTurbine<CartUiState>.awaitSuccessMatching(
        predicate: (CartUiState.Success) -> Boolean
    ): CartUiState.Success {
        while (true) {
            when (val item = awaitItem()) {
                is CartUiState.Success -> if (predicate(item)) return item
                is CartUiState.Error -> error("Unexpected error: ${item.message}")
                is CartUiState.Loading -> Unit
            }
        }
    }
}