package com.artemiod.cursotestingandroid.detail.presentation

import app.cash.turbine.test
import com.artemiod.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import com.artemiod.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeProduct = FakeProductRepository()
    private val fakeCart = FakeCartItemRepository()
    private val fakePromotion = FakePromotionRepository()
    private val fakeClock = FakeSystemClock()

    private fun createViewModel(): ProductDetailViewModel {
        val getProductDetailWithPromotionUseCase = GetProductDetailWithPromotionUseCase(
            productRepository = fakeProduct,
            promotionRepository = fakePromotion,
            getPromotionForProduct = GetPromotionForProduct(),
            clock = fakeClock
        )
        val addToCartUseCase = AddToCartUseCase(
            cartItemRepository = fakeCart,
            productRepository = fakeProduct
        )

        return ProductDetailViewModel(
            getProductDetailWithPromotionUseCase = getProductDetailWithPromotionUseCase,
            addToCartUseCase = addToCartUseCase
        )
    }

    @Test
    fun `given valid product id when load product then emits item`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "id1"
            val p1 = product { withId(productId); withName("erich") }
            fakeProduct.setProducts(listOf(p1))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()
                // When
                viewModel.loadProduct(productId)

                // Then
                val finalState = awaitItem()
                assertFalse("El estado ya no debe estar cargando", finalState.isLoading)
                assertEquals(
                    "Debe tener id $productId",
                    productId,
                    finalState.item?.product?.id
                )
                assertEquals(
                    "Debe tener nombre erich",
                    "erich",
                    finalState.item?.product?.name
                )
                cancelAndConsumeRemainingEvents()
            }
        }


    @Test
    fun `given missing product id when load product then ends item null`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            fakeProduct.setProducts(emptyList())
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()
                // When
                viewModel.loadProduct("id1")

                // Then
                val state = awaitItem()
                assertNull("El estado debe ser null", state.item)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given loaded product when add to cart success then emits success event`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "id1"
            val p = product { withId(productId); withStock(10) }
            fakeProduct.setProducts(listOf(p))
            val viewModel = createViewModel()

            viewModel.uiState.test {
                viewModel.loadProduct(productId)
                // Saltamos el estado inicial (Loading) y esperamos al Success
                skipItems(1)
                val loadedState = awaitItem()
                assertNotNull("El producto debe haberse cargado", loadedState.item)

                //Ahora que sabemos que el producto está ahí, probamos el evento
                viewModel.events.test {
                    // When
                    viewModel.addToCart()

                    // Then
                    val result = awaitItem()
                    assertEquals(ProductDetailEvent.SUCCESS_ADD_TO_CART, result)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given loaded product withouth stock when add to cart then emits insufficient stock error`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "id1"
            val p = product { withId(productId); withStock(0) }
            fakeProduct.setProducts(listOf(p))
            val viewModel = createViewModel()

            viewModel.uiState.test {
                viewModel.loadProduct(productId)
                // Saltamos el estado inicial (Loading) y esperamos al Success
                skipItems(1)
                val loadedState = awaitItem()
                assertNotNull("El producto debe haberse cargado", loadedState.item)

                //Ahora que sabemos que el producto está ahí, probamos el evento
                viewModel.events.test {
                    // When
                    viewModel.addToCart()

                    // Then
                    val result = awaitItem()
                    assertEquals(ProductDetailEvent.INSUFFICIENT_STOCK, result)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

}