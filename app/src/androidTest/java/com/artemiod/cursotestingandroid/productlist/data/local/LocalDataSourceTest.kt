package com.artemiod.cursotestingandroid.productlist.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.core.builder.cartItemEntity
import com.artemiod.cursotestingandroid.core.builder.productEntity
import com.artemiod.cursotestingandroid.core.builder.promotionEntity
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDataSourceTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var localDataSource: LocalDataSource

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()
        localDataSource = LocalDataSource(
            database.productDao(),
            database.promotionDao(),
            database.cartItemDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // *** 📦 Products ***

    @Test
    fun givenProducts_whenSaveAndGetAll_thenReturnsPersistedProduct() = runTest {
        // given
        val products = listOf(
            productEntity { withId("id1") },
            productEntity { withId("id2") },
        )

        // when
        localDataSource.saveProducts(products) // ⚠️ la funcion que testiamos
        val result = localDataSource.getAllProducts().first()

        // then
        assertEquals("Debe haber dos productos", 2, result.size)
    }

    @Test
    fun givenSavedProduct_whenGetProductById_thenReturnsCorrectProduct() = runTest {
        // given
        val products = listOf(
            productEntity { withId("id1"); withName("leche") },
            productEntity { withId("id2") },
        )
        localDataSource.saveProducts(products)

        // when
        val result = localDataSource.getProductById("id1").first() // ⚠️ la funcion que testiamos

        // then
        assertNotNull("Debe existir", result)
        assertEquals("Debe ser el mismo nombre", "leche", result?.name)
    }

    @Test
    fun givenThreeProducts_whenGetProductsByIds_thenEmitsRequestedSubset() = runTest {
        // given
        val products = listOf(
            productEntity { withId("id1") },
            productEntity { withId("id2") },
            productEntity { withId("id3") },
        )
        localDataSource.saveProducts(products)

        // when
        val result =
            localDataSource.getProductsByIds(setOf("id1", "id3"))
                .first() // ⚠️ la funcion que testiamos

        // then
        assertEquals("Debe haber dos productos", 2, result.size)
        assertTrue("Debe contener id1", result.any { it.id == "id1" })
        assertTrue("Debe contener id3", result.any { it.id == "id3" })
        assertTrue("No Debe contener id2", result.none { it.id == "id2" })
    }

    // *** 🏷️ Promotions ***

    @Test
    fun givenPromotions_whenSaveAndGetAll_thenReturnsPersistedPromotions() = runTest {
        // given
        val promotions = listOf(
            promotionEntity { withId("id1") },
            promotionEntity { withId("id2"); withProductIds("""["id1"]""") },
        )

        // when
        localDataSource.savePromotions(promotions) // ⚠️ la funcion que testiamos
        val result = localDataSource.getAllPromotions().first()

        // then
        assertEquals("Debe haber dos promociones", 2, result.size)

    }

    // *** 🛒 Carrito ***

    @Test
    fun givenCartItem_whenInsertCartItem_thenEmitsSuccessAndItemSaved() = runTest {
        // given
        val cartItem = cartItemEntity { withId("id1"); withQuantity(2) }

        // when
        val result = localDataSource.insertCartItem(cartItem) // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)

        val items = localDataSource.getAllCartItems().first()

        assertTrue("Debe tener un elemento", 1 == items.size)
        assertEquals("Debe ser el mismo id", "id1", items.first().productId)
        assertEquals("Debe ser la misma cantidad", 2, items.first().quantity)
    }

    @Test
    fun givenExistingItem_whenUpdateCartItem_thenEmitsSuccessAndCartItemUpdated() = runTest {
        // given
        val id = "id"
        val cartItem = cartItemEntity { withId(id); withQuantity(2) }
        localDataSource.insertCartItem(cartItem)

        val updatedQuantity = 3
        val updatedCartItem = cartItemEntity { withId(id); withQuantity(updatedQuantity) }

        // when
        val result = localDataSource.updateCartItem(updatedCartItem) // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)

        val item = localDataSource.getCartItemById(id)
        assertNotNull("Debe existir", item)
        assertEquals("Debe ser la misma cantidad", updatedQuantity, item?.quantity)
        assertTrue("Debe ser el mismo id", id == item?.productId)
    }

    @Test
    fun givenCartItem_whenDeleteCartItem_thenEmitsSuccessAndCartIsEmpty() = runTest {
        // given
        val cartItem = cartItemEntity { withId("id1"); withQuantity(2) }
        localDataSource.insertCartItem(cartItem)

        // when
        val result = localDataSource.deleteCartItem(cartItem) // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)

        val items = localDataSource.getAllCartItems().first()

        assertTrue("Debe estar vacío", items.isEmpty())
    }

    @Test
    fun givenMultipleCartItems_whenClearCart_thenEmitsSuccessAndCartIsEmpty() = runTest {
        // given
        val cartItem1 = cartItemEntity { withId("id1"); withQuantity(2) }
        val cartItem2 = cartItemEntity { withId("id2"); withQuantity(20) }
        val cartItem3 = cartItemEntity { withId("id3"); withQuantity(51) }
        localDataSource.insertCartItem(cartItem1)
        localDataSource.insertCartItem(cartItem2)
        localDataSource.insertCartItem(cartItem3)

        // when
        val result = localDataSource.clearCart() // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)

        val items = localDataSource.getAllCartItems().first()

        assertTrue("Debe estar vacío", items.isEmpty())
    }
}