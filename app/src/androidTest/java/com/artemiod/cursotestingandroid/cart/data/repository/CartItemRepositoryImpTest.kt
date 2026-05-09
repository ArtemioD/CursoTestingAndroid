package com.artemiod.cursotestingandroid.cart.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartItemRepositoryImpTest {

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var cartItemRepository: CartItemRepository


    @Before
    fun setUp() = runTest {
        hilt.inject()

        cartItemRepository.clearCart()

        val productJson = """
        {"products": [
            {"id": "p1", "name": "Pan", "description": "Pan fresco", "category": "Comida", "priceCents": 150, "stock": 10},
            {"id": "p2", "name": "Leche", "description": "Leche entera", "category": "Lacteos", "priceCents": 200, "stock": 5}
        ]}
    """.trimIndent()

        mockWebServer.server.enqueue(MockResponse().setBody(productJson).setResponseCode(200))
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenEmptyCart_whenGetCartItems_thenEmitsEmptyList() = runTest {
        val items = cartItemRepository.getCartItems().first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun givenEmptyCart_whenAddToCart_thenGetCartItemsContainsItems() = runTest {
        cartItemRepository.addToCart("id1", 2)

        val items = cartItemRepository.getCartItems().first()
        assertEquals(1, items.size)
        assertEquals("id1", items.first().productId)
        assertEquals(2, items[0].quantity)
    }

    @Test
    fun givenExistingItem_whenAddToCartAgain_thenQuantityIsUpdated() = runTest {
        cartItemRepository.addToCart("id1", 1)
        cartItemRepository.addToCart("id1", 2)

        val items = cartItemRepository.getCartItems().first()
        assertEquals(1, items.size)
        assertEquals("id1", items.first().productId)
        assertEquals(3, items[0].quantity)
    }

    @Test(expected = AppError.NotFoundError::class)
    fun givenEmptyCart_whenRemoveFromCart_thenThrowsNotFoundError() = runTest {
        cartItemRepository.removeFromCart("errr")
    }

    @Test
    fun givenItemInCart_whenUpdateQuantity_thenQuantityIsUpdated() = runTest {
        cartItemRepository.addToCart("id1", 1)
        cartItemRepository.updateQuantity("id1", 5)

        val items = cartItemRepository.getCartItems().first()
        assertEquals(1, items.size)
        assertEquals("id1", items.first().productId)
        assertEquals(5, items.first().quantity)
    }

    @Test
    fun givenItemInCart_whenRemoveFromCart_thenQuantityIsEmpty() = runTest {
        cartItemRepository.addToCart("id1", 1)
        cartItemRepository.removeFromCart("id1")

        val items = cartItemRepository.getCartItems().first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun givenMultipleItemsInCart_whenClearCart_thenCartIsEmpty() = runTest {
        cartItemRepository.addToCart("id1", 1)
        cartItemRepository.addToCart("id2", 1)
        cartItemRepository.clearCart()

        val items = cartItemRepository.getCartItems().first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun givenItemInCart_whenGetCartItemById_thenReturnsItem() = runTest {
        cartItemRepository.addToCart("id1", 1)

        val item = cartItemRepository.getCartItemById("id1")

        assertNotNull(item)
        assertTrue(item!!.productId == "id1")
        assertEquals(1, item.quantity)
    }

}