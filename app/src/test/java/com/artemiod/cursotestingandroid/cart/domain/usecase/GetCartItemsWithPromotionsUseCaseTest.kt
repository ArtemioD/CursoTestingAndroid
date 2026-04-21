package com.artemiod.cursotestingandroid.cart.domain.usecase

import com.artemiod.cursotestingandroid.core.builders.cartItem
import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.builders.promotion
import com.artemiod.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import kotlin.collections.first

class GetCartItemsWithPromotionsUseCaseTest {

    private val clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T00:00:00Z")) }

    private fun useCase(
        cart: FakeCartItemRepository = FakeCartItemRepository(),
        products: FakeProductRepository = FakeProductRepository(),
        promos: FakePromotionRepository = FakePromotionRepository(),
        clock: FakeSystemClock = this.clock
    ) =
        GetCartItemsWithPromotionsUseCase(
            cartItemRepository = cart,
            productRepository = products,
            promotionRepository = promos,
            getPromotionForProduct = GetPromotionForProduct(),
            clock = clock
        )

    @Test
    fun `given empty cart when invoke then return empty list`() = runTest {
        // Given
        val cart = FakeCartItemRepository().apply { setCartItems(emptyList()) }

        // When
        val result = (useCase(cart = cart)()).first()

        // Then
        assertTrue("El estado inicial del carrito debe estar vacío", result.isEmpty())
        assert(result.isEmpty())
    }

    @Test
    fun `given existing cart item with active promotion when invoke then returns item with promotion`() =
        runTest {
            // Given
            val productId = "product-id"
            val product = product {
                withId(productId)
            }
            val now = clock.now()
            val promo = promotion {
                withProductIds(listOf(productId))
                withStartTime(now.minusSeconds(10))
                withEndTime(now.plusSeconds(10))
            }

            val cartItem = cartItem {
                withId(productId)
                withQuantity(2)
            }
            val cart = FakeCartItemRepository().apply { setCartItems(listOf(cartItem)) }
            val products = FakeProductRepository().apply { setProducts(listOf(product)) }
            val promotions = FakePromotionRepository().apply { setPromotions(listOf(promo)) }

            // When
            val result = useCase(cart, products, promotions, clock)().first()

            // Then
            assertEquals("Debe haber un elemento en el carrito", 1, result.size)
            assertNotNull("Debe haber una promoción activa", result.first().item.promotion)
        }

    @Test
    fun `given cart item wirhout maching product when invoke then skip item`() = runTest {
        // Given
        val catr = FakeCartItemRepository().apply {
            setCartItems(listOf(cartItem { withId("ghost-id") }))
        }
        val products = FakeProductRepository().apply {
            setProducts(listOf(product { withId("other-id") }))
        }

        // When
        val result = useCase(catr, products, FakePromotionRepository(), clock)().first()

        // Then
        assertTrue("El carrito debe estar vacío", result.isEmpty())
    }

    @Test
    fun `given promotion ending exactly now when invoke then it must be included`() = runTest {
        // Given
        val now = clock.now()
        val productId = "product-id"
        val product = product {
            withId(productId)
        }
        val promoPromotion = promotion {
            withProductIds(listOf(productId))
            withStartTime(now.minusSeconds(100))
            withEndTime(now)
        }

        val cart =
            FakeCartItemRepository().apply { setCartItems(listOf(cartItem { withId(productId) })) }
        val products = FakeProductRepository().apply { setProducts(listOf(product)) }
        val promotions = FakePromotionRepository().apply { setPromotions(listOf(promoPromotion)) }

        // When
        val result = useCase(cart, products, promotions, clock)().first()

        // Then
        assertEquals("Debe haber un elemento en el carrito", 1, result.size)
        assertNotNull("Debe haber una promoción activa", result.first().item.promotion)
    }

    @Test
    fun `given expired promotion when invoke then item remains but without promotion`() = runTest {
        // Given
        val now = clock.now()
        val productId = "product-id"
        val product = product {
            withId(productId)
        }
        val endPromotion = promotion {
            withProductIds(listOf(productId))
            withStartTime(now.minusSeconds(100))
            withEndTime(now.minusSeconds(1))
        }

        val cart =
            FakeCartItemRepository().apply { setCartItems(listOf(cartItem { withId(productId) })) }
        val products = FakeProductRepository().apply { setProducts(listOf(product)) }
        val promotions = FakePromotionRepository().apply { setPromotions(listOf(endPromotion)) }

        // When
        val result = useCase(cart, products, promotions, clock)().first()

        // Then
        assertEquals("Debe haber un elemento en el carrito", 1, result.size)
        assertNull("No debe haber una promoción activa", result.first().item.promotion)
    }

    @Test
    fun `given active promotion when time advances then flow emits update list with promotion`() = runTest {
        // Given
        val now = clock.now()
        val productId = "product-id"
        val product = product {
            withId(productId)
        }
        val promotion = promotion {
            withProductIds(listOf(productId))
            withStartTime(now.minusSeconds(100))
            withEndTime(now.plusSeconds(5))
        }

        val cart =
            FakeCartItemRepository().apply { setCartItems(listOf(cartItem { withId(productId) })) }
        val products = FakeProductRepository().apply { setProducts(listOf(product)) }
        val promotions = FakePromotionRepository().apply { setPromotions(listOf(promotion)) }

        // When
        val myUseCase = useCase(cart, products, promotions, clock)()
        val firstEmission = myUseCase.first()

        // Then
        assertEquals("Debe haber un elemento en el carrito", 1, firstEmission.size)
        assertNotNull("Debe haber una promoción activa", firstEmission.first().item.promotion)

        // Advance time
        clock.advanceTime(6)
        val secondEmission = myUseCase.first()

        assertEquals("Debe haber un elemento en el carrito", 1, firstEmission.size)
        assertNull("No debe haber una promoción activa", secondEmission.first().item.promotion)

    }


}