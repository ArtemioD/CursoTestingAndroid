package com.artemiod.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.artemiod.cursotestingandroid.core.builder.productEntity
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()

        dao = database.productDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyDatabase_whenGetAllProducts_thenEmitsEmptyList() = runTest {
        // given
        // when
        val products = dao.getAllProducts().first()

        // then
        assertTrue("Debe estar vacío", products.isEmpty())
    }

    @Test
    fun givenInsertedProduct_whenGetProductById_thenEmitsProduct() = runTest {
        // given
        val id = "id"
        val p = productEntity { withId(id) }
        dao.insertProducts(listOf(p))

        // when
        val product = dao.getProductsById(id).first()

        // then
        assertNotNull("Debe existir", product)
        assertEquals("Debe ser el mismo id", id, p.id)
    }

    @Test
    fun givenThreeProducts_whenGetProductsByIds_thenEmitsRequestedSubset() = runTest {
        // given
        dao.insertProducts(
            listOf(
                productEntity { withId("id1") },
                productEntity { withId("id2") },
                productEntity { withId("id3") },
            )
        )

        // when
        val products = dao.getProductsByIds(listOf("id1", "id3")).first()

        // then
        assertEquals("Debe haber dos productos", 2, products.size)
        assertTrue("Debe contener id1", products.any { it.id == "id1" })
        assertTrue("Debe contener id3", products.any { it.id == "id3" })
        assertTrue("No Debe contener id2", products.none { it.id == "id2" })
    }

    @Test
    fun givenOldProducts_whenReplaceAll_thenOnlyNewProductsRemain() = runTest {
        // given
        dao.insertProducts(
            listOf(
                productEntity { withId("old-id1") },
                productEntity { withId("old-id2") },
            )
        )

        // when
        val newProducts = listOf(
            productEntity { withId("new-id1") },
            productEntity { withId("new-id2") },
            productEntity { withId("new-id3") },
        )

        dao.replaceAll(newProducts)
        val result = dao.getAllProducts().first()

        // then
        assertEquals("Debe haber tres productos", 3, result.size)
        assertTrue("Debe contener id1", result.any { it.id == "new-id1" })
        assertTrue("Debe contener id3", result.any { it.id == "new-id2" })
        assertTrue("Debe contener id3", result.any { it.id == "new-id3" })
        assertTrue(
            "No Debe contener old-id1 o old-id2",
            result.none { it.id == "old-id1" || it.id == "old-id2" })
    }

    @Test
    fun givenExistingProducts_whenInsertSameIdWithDifferentData_thenReplaceOldData() = runTest {
        // given
        val id = "id"
        dao.insertProducts(
            listOf(
                productEntity { withId(id); withName("pan") }
            ))

        // when
        dao.insertProducts(
            listOf(
                productEntity { withId(id); withName("leche") }
            ))

        val result = dao.getAllProducts().first()

        // then
        assertTrue("Debe ser el mismo id", result.size == 1)
        assertEquals("Debe ser el mismo nombre", "leche", result.first().name)
    }

    @Test
    fun givenFlowSubscribed_whenInsertAfterSubscribe_thenEmitsUpdateList() = runTest {
        dao.getAllProducts().test {
            // given
            val initialList = awaitItem()
            assertTrue(initialList.isEmpty())

            // when
            dao.insertProducts(listOf(productEntity { withId("id1") }))

            // then
            val updatedList = awaitItem()
            assertEquals("Debe haber un producto", 1, updatedList.size)
            assertEquals("Debe haber id1 en la lista", "id1", updatedList.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}