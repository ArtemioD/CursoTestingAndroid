package com.artemiod.cursotestingandroid.productlist.domain.usecase

import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.builders.promotion
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class GetProductsUseCaseTest {

    private fun useCase(
        products: FakeProductRepository = FakeProductRepository(),
        promos: FakePromotionRepository = FakePromotionRepository(),
        settings: FakeSettingsRepository = FakeSettingsRepository(),
        clock: FakeSystemClock = FakeSystemClock()
    ) = GetProductsUseCase(
        productRepository = products,
        promotionRepository = promos,
        getPromotionForProduct = GetPromotionForProduct(),
        settingsRepository = settings,
        clock = clock
    )

    @Test
    fun `given promotion ending now when invoke then it should be included`() = runTest {
        // Given
        val now = Instant.parse("2026-04-03T00:00:00Z")
        val clock = FakeSystemClock().apply { setTime(now) }

        val productId = "product-id"
        val products = product {
            withId(productId)
        }
        val promos = promotion {
            withProductIds(listOf(productId))
            withStartTime(now.minusSeconds(60))
            withEndTime(now)
        }
        val productRepository = FakeProductRepository().apply { setProducts(listOf(products)) }
        val promotionRepository = FakePromotionRepository().apply { setPromotions(listOf(promos)) }

        // When
        val result = (useCase(
            products = productRepository,
            promos = promotionRepository,
            clock = clock
        )()).first()

        // Then
        assertNotNull(result.first())
    }

    @Test
    fun `given active promotion when time advances then promotion should no be longer be returned`()= runTest {
        // Given
        val now = Instant.parse("2026-04-03T00:00:00Z")
        val clock = FakeSystemClock().apply { setTime(now) }

        val productId = "product-id"
        val products = product {
            withId(productId)
        }
        val promos = promotion {
            withProductIds(listOf(productId))
            withStartTime(now)
            withEndTime(now.plusSeconds(5))
        }
        val productRepository = FakeProductRepository().apply { setProducts(listOf(products)) }
        val promotionRepository = FakePromotionRepository().apply { setPromotions(listOf(promos)) }

        // When
        val firstResult = (useCase(
            products = productRepository,
            promos = promotionRepository,
            clock = clock
        )()).first()

        clock.advanceTime(6)
        val secondResult = (useCase(
            products = productRepository,
            promos = promotionRepository,
            clock = clock
        )()).first()

        // Then
        assertNotNull(firstResult.first().promotion)
        assert(firstResult.first().promotion != null)
        assertNull(secondResult.first().promotion)
        assert(secondResult.first().promotion == null)
    }

    @Test
    fun `given inStockOnly enabled when product goes out of stock then it should be filtered`() = runTest {
        // Given
        val productId = "product-id"
        val products = product {
            withId(productId)
            withStock(0)
        }
        val settings = FakeSettingsRepository().apply { setInStockOnly(true) }
        val productRepository = FakeProductRepository().apply { setProducts(listOf(products)) }

        val myUseCase = useCase(
            products = productRepository,
            settings = settings
        )

        // When
        val result = myUseCase().first()

        // Then
        assert(result.isEmpty())
    }

}