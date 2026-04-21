package com.artemiod.cursotestingandroid.cart.presentation

import app.cash.turbine.test
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.artemiod.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.artemiod.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.builders.cartItem
import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.domain.util.Clock
import com.artemiod.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.junit.JUnitAsserter.assertTrue

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        productRepository: ProductRepository = FakeProductRepository(),
        cartItemRepository: CartItemRepository = FakeCartItemRepository(),
        promotionRepository: PromotionRepository = FakePromotionRepository(),
        clock: Clock = FakeSystemClock()
    ): CartViewModel {

        val getCartSummaryUseCase = GetCartSummaryUseCase(
            cartItemRepository = cartItemRepository,
            productRepository = productRepository,
            promotionRepository = promotionRepository,
            getPromotionForProduct = GetPromotionForProduct(),
            clock = clock,
        )

        val updateCartItemUseCase = UpdateCartItemUseCase(
            cartItemRepository = cartItemRepository,
            productRepository = productRepository
        )

        val getCartItemsWithPromotionsUseCase = GetCartItemsWithPromotionsUseCase(
            cartItemRepository = cartItemRepository,
            productRepository = productRepository,
            promotionRepository = promotionRepository,
            getPromotionForProduct = GetPromotionForProduct(),
            clock = clock,
        )

        return CartViewModel(
            cartItemRepository,
            getCartSummaryUseCase,
            updateCartItemUseCase,
            getCartItemsWithPromotionsUseCase
        )
    }

    @Test
    fun `given cart data when initialized then emits success state`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "1"
            val p = product { withId(productId); withName("Pan"); withPrice(2.0) }
            val item = cartItem { withId(productId); withQuantity(3) }

            val fakeProduct = FakeProductRepository().apply { setProducts(listOf(p)) }
            val fakeCartItem = FakeCartItemRepository().apply { setCartItems(listOf(item)) }

            // When
            val viewModel = createViewModel(
                productRepository = fakeProduct,
                cartItemRepository = fakeCartItem
            )

            // Then
            viewModel.uiState.test {
                val state = awaitItem() as CartUiState.Success
                assertEquals("Debe tener un solo producto", 1, state.cartItems.size)
                assertEquals("Debe valer $6.0", 6.0, state.summary?.subtotal)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given quantity one when decrease quantity then removes item from cart`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "1"
            val p = product { withId(productId); withStock(5); withPrice(2.0) }
            val item = cartItem { withId(productId); withQuantity(1) }

            val fakeProduct = FakeProductRepository().apply { setProducts(listOf(p)) }
            val fakeCartItem = FakeCartItemRepository().apply { setCartItems(listOf(item)) }
            val viewModel = createViewModel(
                productRepository = fakeProduct,
                cartItemRepository = fakeCartItem
            )

            viewModel.uiState.test {
                awaitItem()
                // When
                viewModel.decreaseQuantity(productId, 1)

                // Then
                val state = awaitItem() as CartUiState.Success
                assertTrue("Debe estar carrrito vacio", state.cartItems.isEmpty())
                assertEquals(
                    "Debe tener un subtotal de $0.0",
                    0.0,
                    state.summary?.finalTotal ?: 0.0,
                    0.001
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given insufficent stock when update quantity then emits error event`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "1"
            val p = product { withId(productId); withStock(2) }
            val item = cartItem { withId(productId); withQuantity(1) }

            val fakeProduct = FakeProductRepository().apply { setProducts(listOf(p)) }
            val fakeCartItem = FakeCartItemRepository().apply { setCartItems(listOf(item)) }
            val viewModel = createViewModel(
                productRepository = fakeProduct,
                cartItemRepository = fakeCartItem
            )

            viewModel.event.test {
                // When
                viewModel.updateCartItem(productId, 5)

                // Then
                val event = awaitItem()
                assertTrue("Debe mostrar un mensaje", event is CartEvent.ShowMessage)
                cancelAndConsumeRemainingEvents()
            }
        }

}