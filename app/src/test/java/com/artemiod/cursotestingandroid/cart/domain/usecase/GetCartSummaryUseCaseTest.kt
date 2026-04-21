package com.artemiod.cursotestingandroid.cart.domain.usecase

import com.artemiod.cursotestingandroid.core.builders.cartItem
import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.builders.promotion
import com.artemiod.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import com.artemiod.cursotestingandroid.productlist.domain.model.PromotionType
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetCartSummaryUseCaseTest {

    private lateinit var clock: FakeSystemClock
    private lateinit var cartRepository: FakeCartItemRepository
    private lateinit var productRepository: FakeProductRepository
    private lateinit var promoRepository: FakePromotionRepository

    @Before
    fun setUp() {
        clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }
        cartRepository = FakeCartItemRepository()
        productRepository = FakeProductRepository()
        promoRepository = FakePromotionRepository()
    }

    private fun useCase() = GetCartSummaryUseCase(
        cartItemRepository = cartRepository,
        productRepository = productRepository,
        promotionRepository = promoRepository,
        getPromotionForProduct = GetPromotionForProduct(),
        clock = clock
    )

    @Test
    fun `given percent promotion when invoke then calculate correctly`() = runTest {
        // Given
        val productId = "p1"
        val product = product {
            withId(productId)
            withPrice(100.0)
        }
        val promo = promotion {
            withProductIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }
        val cartItem = cartItem {
            withId(productId)
            withQuantity(2)
        }

        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))
        cartRepository.setCartItems(listOf(cartItem))

        // When
        val summary = (useCase()()).first()

        // Then
        assertEquals("Debe calcular el subtotal correctamente", 200.0, summary.subtotal)
        assertEquals("Debe calcular el descuento correctamente", 20.0, summary.discountTotal)
        assertEquals("Debe calcular el total correctamente", 180.0, summary.finalTotal)
    }

    @Test
    fun `given 3 items in 2x1 promotion when invoke then only discounts 1 unit`() = runTest {
        // Given
        val productId = "p1"
        val product = product {
            withId(productId)
            withPrice(100.0)
        }
        val promo = promotion {
            withProductIds(listOf(productId))
            withType(PromotionType.BUY_X_PAY_Y)
            withBayQuantity(2)
            withValue(1.0)
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }
        val cartItem = cartItem {
            withId(productId)
            withQuantity(3)
        }

        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))
        cartRepository.setCartItems(listOf(cartItem))

        // When
        val summary = (useCase()()).first()

        // Then
        assertEquals("Debe calcular el subtotal correctamente", 300.0, summary.subtotal)
        assertEquals("Debe calcular el descuento correctamente", 100.0, summary.discountTotal)
        assertEquals("Debe calcular el total correctamente", 200.0, summary.finalTotal)
    }

    @Test
    fun `given multiple products with different promotions when invoke then sums all correctly`() =
        runTest {
            // Given
            val now = clock.now()
            val p1 = product { withId("p1"); withPrice(100.0) } // con promo
            val p2 = product { withId("p2");withPrice(50.0) } // sin promo

            val promoPercent = promotion {
                withProductIds(listOf("p1"))
                withType(PromotionType.PERCENT)
                withValue(10.0)
                withStartTime(now.minusSeconds(10))
                withEndTime(now.plusSeconds(10))
            }

            val cart = listOf(
                cartItem { withId("p1"); withQuantity(1) },
                cartItem { withId("p2"); withQuantity(1) }
            )

            productRepository.setProducts(listOf(p1, p2))
            promoRepository.setPromotions(listOf(promoPercent))
            cartRepository.setCartItems(cart)

            // When
            val summary = (useCase()()).first()

            // Then
            assertEquals("Debe calcular el subtotal correctamente", 150.0, summary.subtotal)
            assertEquals("Debe calcular el descuento correctamente", 10.0, summary.discountTotal)
            assertEquals("Debe calcular el total correctamente", 140.0, summary.finalTotal)
        }

    @Test
    fun `given expired promotion when invoke then discount is zero`() = runTest {
        // Given
        val now = clock.now()
        val p1 = product { withId("p1"); withPrice(100.0) }
        val promoPercent = promotion {
            withProductIds(listOf("p1"))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(now.minusSeconds(10))
            withEndTime(now.minusSeconds(5))
        }

        productRepository.setProducts(listOf(p1))
        promoRepository.setPromotions(listOf(promoPercent))
        cartRepository.setCartItems( listOf(cartItem { withId("p1"); withQuantity(1)}))

        // When
        val summary = (useCase()()).first()

        // Then
        assertEquals("Debe calcular el subtotal correctamente", 100.0, summary.subtotal)
        assertEquals("Debe calcular el descuento correctamente", 0.0, summary.discountTotal)
        assertEquals("Debe calcular el total correctamente", 100.0, summary.finalTotal)
    }

    @Test
    fun `given active promotion when time advances then summary updates automatically`() = runTest {
        // Given
        val now = clock.now()
        val p1 = product { withId("p1"); withPrice(100.0) }
        val promoPercent = promotion {
            withProductIds(listOf("p1"))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(now.minusSeconds(10))
            withEndTime(now.plusSeconds(5))
        }

        productRepository.setProducts(listOf(p1))
        promoRepository.setPromotions(listOf(promoPercent))
        cartRepository.setCartItems( listOf(cartItem { withId("p1"); withQuantity(1)}))

        // When
        val summaryFlow = useCase()()

        // Then
        assertEquals("Debe calcular el subtotal correctamente", 100.0, summaryFlow.first().subtotal)
        assertEquals("Debe calcular el descuento correctamente", 10.0, summaryFlow.first().discountTotal)
        assertEquals("Debe calcular el total correctamente", 90.0, summaryFlow.first().finalTotal)

        // Advance time
        clock.advanceTime(6)

        assertEquals("Debe calcular el subtotal correctamente", 100.0, summaryFlow.first().subtotal)
        assertEquals("Debe calcular el descuento correctamente", 0.0, summaryFlow.first().discountTotal)
        assertEquals("Debe calcular el total correctamente", 100.0, summaryFlow.first().finalTotal)
    }


}