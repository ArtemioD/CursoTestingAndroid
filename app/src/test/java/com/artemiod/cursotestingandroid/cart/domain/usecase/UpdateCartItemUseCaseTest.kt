package com.artemiod.cursotestingandroid.cart.domain.usecase

import com.artemiod.cursotestingandroid.core.builders.cartItem
import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UpdateCartItemUseCaseTest {

    @Test
    fun given_negative_quantity_when_invokes_then_throws_QuantityMustBePositive() = runTest {
        // Given
        val fakeProductRepository = FakeProductRepository()
        val fakeCartRepository = FakeCartItemRepository()

        val useCase = UpdateCartItemUseCase(fakeCartRepository, fakeProductRepository)

        // When
        val exception = runCatching {
            useCase("product-id", -1)
        }.exceptionOrNull()

        // Then
        assertEquals(exception, AppError.Validation.QuantityMustBePositive)
    }

    @Test
    fun given_zero_quantity_when_invokes_then_removes_items_from_cart() = runTest {
        // Given
        val productId = "id-test-1"
        val product = product {
            withId(productId)
        }
        val cartItemProduct = cartItem {
            withId(productId)
            withQuantity(3)
        }
        val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
        val fakeCartItemRepository =
            FakeCartItemRepository().apply { setCartItems(listOf(cartItemProduct)) }

        val useCase = UpdateCartItemUseCase(fakeCartItemRepository, fakeProductRepository)

        // When
        useCase(productId, 0)

        // Then
        val items = fakeCartItemRepository.getCartItems().first()
        assertTrue(items.isEmpty())
        assertEquals(0, items.size)
    }

    @Test
    fun given_missing_product_when_invoke_then_throws_NotFoundError() = runTest {
        // Given
        val fakeProductRepository = FakeProductRepository().apply { setProducts(emptyList()) }
        val fakeCartItemRepository = FakeCartItemRepository()

        val useCase = UpdateCartItemUseCase(fakeCartItemRepository, fakeProductRepository)

        // When
        val exception = runCatching {
            useCase("product-id", 1)
        }.exceptionOrNull()

        // Then
        assertEquals(exception, AppError.NotFoundError)
    }

    @Test
    fun given_requested_quantity_greater_than_stock_when_invoke_then_throws_InsufficientStock() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product = product {
                withId(productId)
                withStock(3)
            }
            val cartItem = cartItem {
                withId(productId)
                withQuantity(1)
            }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakeCartItemRepository =
                FakeCartItemRepository().apply { setCartItems(listOf(cartItem)) }


            val useCase = UpdateCartItemUseCase(fakeCartItemRepository, fakeProductRepository)

            // When
            val exception = runCatching { useCase(productId, 5) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.InsufficientStock)
            assertEquals(3, (exception as AppError.Validation.InsufficientStock).availableStock)
        }

    @Test
    fun given_valid_product_and_quantity_when_invoke_then_updates_cart_item() = runTest {
        // Given
        val productId = "id-test-1"
        val product = product {
            withId(productId)
            withStock(20)
        }
        val cartItem = cartItem {
            withId(productId)
            withQuantity(1)
        }
        val fakeProductRepository =
            FakeProductRepository().apply { setProducts(listOf(product)) }
        val fakeCartItemRepository =
            FakeCartItemRepository().apply { setCartItems(listOf(cartItem)) }

        val useCase = UpdateCartItemUseCase(fakeCartItemRepository, fakeProductRepository)

        // When
        useCase(productId, 5)

        // Then
        val items = fakeCartItemRepository.getCartItems().first()
        assertEquals(1, items.size)
        assertEquals(5, items.first().quantity)
    }


}


