package com.artemiod.cursotestingandroid.detail.domain.usecase

import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.builders.promotion
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import com.artemiod.cursotestingandroid.productlist.domain.model.PromotionType
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetProductDetailWithPromotionUseCaseTest {

    private lateinit var clock: FakeSystemClock
    private lateinit var productRepository: FakeProductRepository
    private lateinit var promoRepository: FakePromotionRepository

    @Before
    fun setUp() {
        clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }
        productRepository = FakeProductRepository()
        promoRepository = FakePromotionRepository()
    }

    private fun useCase() = GetProductDetailWithPromotionUseCase(
        productRepository = productRepository,
        promotionRepository = promoRepository,
        getPromotionForProduct = GetPromotionForProduct(),
        clock = clock
    )

    @Test
    fun `given active promotion when invoke then return product with promotion`() = runTest {
        // Given
        val productId = "p1"
        val product = product { withId(productId) }
        val promo = promotion {
            withProductIds(listOf(productId))
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }

        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))

        // When
        val result = useCase()(productId = productId).first()

        // Then
        assertNotNull("Debe existir un producto", result?.product)
        assertNotNull("Debe existir una promoción", result?.promotion)
        assertEquals("Debe ser el mismo producto", productId, result?.product?.id)
    }

    @Test
    fun `given expired promotion when invoke then return product without promotion`() = runTest {
        // Given
        val productId = "p1"
        val product = product { withId(productId) }
        val promo = promotion {
            withProductIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().minusSeconds(1))
        }

        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))

        // When
        val result = useCase()(productId = productId).first()

        // Then
        assertNotNull("Debe existir un producto", result?.product)
        assertNull("Debe ser nulo promoción", result?.promotion)
        assertEquals("Debe ser el mismo producto", productId, result?.product?.id)
    }

    @Test
    fun `given not existing product id when invoke then return null`() = runTest {
        // Given
        val productId = "p1"
        val product = product { withId(productId) }
        val promo = promotion { withProductIds(listOf(productId)) }

        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))

        // When
        val result = useCase()(productId = "ghost-id").first()

        // Then
        assertNull("Debe ser nulo", result)
    }

    /*
    Lo misma comprobacion que de arriba pero una version simplificada
     */
    @Test
    fun `given not existing product id when invoke then return null V2`() = runTest {
        // Given
        productRepository.setProducts(emptyList())

        // When
        val result = useCase()(productId = "ghost-id").first()

        // Then
        assertNull("Debe ser nulo", result)
    }


    @Test
    fun `given product id when invoke then return product not promotion`() = runTest {
        // Given
        val productId = "p1"
        val product = product { withId(productId) }
        val promoId = "promo-id"
        val promo = promotion { withProductIds(listOf(promoId)) }

        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))

        // When
        val result = useCase()(productId = productId).first()

        // Then
        assertNotNull("Debe existir un producto", result?.product)
        assertNull("Debe no haber promoción", result?.promotion)
    }

    @Test
    fun `given product id when time advances then return product with promotion`() = runTest {
        // Given
        val productId = "p1"
        val product = product { withId(productId) }
        val promo = promotion {
            withProductIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(clock.now().plusSeconds(5))
            withEndTime(clock.now().plusSeconds(15))
        }
        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))

        // When
        val result = useCase()(productId = productId)

        // Then
        assertNotNull("Debe existir un producto", result.first()?.product)
        assertNull("Debe ser nulo promoción", result.first()?.promotion)

        // Advance time
        clock.advanceTime(6)

        assertNotNull("Debe existir un producto", result.first()?.product)
        assertNotNull("Debe existir una promoción", result.first()?.promotion)
    }

    @Test
    fun `given active promotion when time advances then product becomes null`() = runTest {
        // Given
        val productId = "p1"
        val product = product { withId(productId) }
        val promo = promotion {
            withProductIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(5))
        }
        productRepository.setProducts(listOf(product))
        promoRepository.setPromotions(listOf(promo))

        // When
        val result = useCase()(productId = productId)

        // Then
        assertNotNull("Debe existir un producto", result.first()?.product)
        assertNotNull("Debe existir una promoción", result.first()?.promotion)

        // Advance time
        clock.advanceTime(6)

        assertNotNull("Debe existir un producto", result.first()?.product)
        assertNull("Debe ser nulo promoción", result.first()?.promotion)
    }

}