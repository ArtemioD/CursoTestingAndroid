package com.artemiod.cursotestingandroid.cart.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.core.builder.cartItemEntity
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartItemDaoTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var dao: CartItemDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()

        dao = database.cartItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyCart_whenGetAllCarItems_thenEmitsEmptyList() = runTest {
        // given
        // when
        val cartItems = dao.getAllCartItems().first()

        // then
        assertTrue("Debe estar vacío", cartItems.isEmpty())
    }

    @Test
    fun givenEmptyCart_whenInsertItem_thenEmitsListWithOneItem() = runTest {
        // given
        val id = "id"
        val quantity = 3
        val cartItem = cartItemEntity { withId(id); withQuantity(quantity) }
        dao.insertCartItem(cartItem)

        // when
        val result = dao.getAllCartItems().first() // ⚠️ la funcion que testiamos

        // then
        assertEquals("Debe haber un elemento", 1, result.size)
        assertEquals("Debe ser #$id", id, result.first().productId)
        assertEquals("Debe tener $quantity unidades", quantity, result.first().quantity)
    }

    @Test
    fun givenInsertedItem_whenGetItemsById_thenEmitsCorrectItem() = runTest {
        // given
        val id = "id"
        val quantity = 3
        val cartItem = cartItemEntity { withId(id); withQuantity(quantity) }
        dao.insertCartItem(cartItem)

        // when
        val result = dao.getCartItemById(id) // ⚠️ la funcion que testiamos

        // then
        assertEquals("Debe ser #$id", id, result?.productId)
        assertEquals("Debe tener $quantity unidades", quantity, result?.quantity)
    }

    @Test
    fun givenEmptyCart_whenGetItemsById_thenReturnsNull() = runTest {
        // given
        val id = "id"

        // when
        val result = dao.getCartItemById(id) // ⚠️ la funcion que testiamos

        // then
        assertNull("Debe ser null", result)
    }

    @Test
    fun givenExistingItem_whenUpdateItemQuantity_thenItemIsUpdated() = runTest {
        // given
        val id = "id"
        val quantity = 3
        val cartItem = cartItemEntity { withId(id); withQuantity(quantity) }
        dao.insertCartItem(cartItem)

        val updatedQuantity = 5
        val updatedCartItem = cartItemEntity { withId(id); withQuantity(updatedQuantity) }
        dao.updateCartItem(updatedCartItem) // ⚠️ la funcion que testiamos

        // when
        val result = dao.getCartItemById(id)

        // then
        assertEquals("Debe ser #$id", id, result?.productId)
        assertEquals("Debe tener $updatedQuantity unidades", updatedQuantity, result?.quantity)
    }

    @Test
    fun givenItemCart_whenDeleteItem_thenCartIsEmpty() = runTest {
        // given
        val id = "id"
        val quantity = 3
        val cartItem = cartItemEntity { withId(id); withQuantity(quantity) }
        dao.insertCartItem(cartItem)

        // when
        dao.deleteCartItem(cartItem) // ⚠️ la funcion que testiamos
        val result = dao.getAllCartItems().first()

        // then
        assertTrue("Debe estar vacío", result.isEmpty())
    }

    @Test
    fun givenCartWithItems_whenClearCart_thenCartIsEmpty() = runTest {
        // given
        val cartItems = listOf(
            cartItemEntity { withId("id1"); withQuantity(1) },
            cartItemEntity { withId("id2"); withQuantity(2) },
            cartItemEntity { withId("id3"); withQuantity(3) }
        )
        cartItems.forEach { dao.insertCartItem(it) }

        dao.clearCart() // ⚠️ la funcion que testiamos

        // when
        val result = dao.getAllCartItems().first()

        // then
        assertTrue("Debe estar vacío", result.isEmpty())
    }

    @Test
    fun givenExistingItemId_whenInsertDuplicate_thenReplaceOldItem() = runTest {
        // given
        val id = "id"
        val quantity = 3
        val cartItem = cartItemEntity { withId(id); withQuantity(quantity) }
        dao.insertCartItem(cartItem)

        val updatedQuantity = 5
        val updatedCartItem = cartItemEntity { withId(id); withQuantity(updatedQuantity) }

        dao.insertCartItem(updatedCartItem) // ⚠️ la funcion que testiamos

        // when
        val result = dao.getCartItemById(id)

        // then
        assertEquals("Debe ser #$id", id, result?.productId)
        assertEquals("Debe tener $updatedQuantity unidades", updatedQuantity, result?.quantity)
    }

}