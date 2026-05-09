package com.artemiod.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.core.builder.promotionEntity
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromotionDaoTest {

    private lateinit var database: MiniMarketDatabase

    private lateinit var dao: PromotionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()
        dao = database.promotionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyListPromotions_whenInsertPromotions_thenEmitsAllPromotions() = runTest {
        // given
        val promotions = listOf(
            promotionEntity { withId("id1"); withPercent(20) },
            promotionEntity { withId("id2") },
        )

        // when
        dao.insertPromotions(promotions) // ⚠️ la funcion que testiamos

        // then
        val result = dao.getAllPromotions().first()
        assertEquals("Debe haber dos promociones", 2, result.size)
        assertEquals("Debe haber tener 20%", 20, result.find { it.id == "id1" }?.percent)
    }

    @Test
    fun givenPromotions_whenInsertMorePromotions_thenEmitsAllPromotions() = runTest {
        // given
        val promotions = listOf(
            promotionEntity { withId("id1") },
            promotionEntity { withId("id2") },
        )
        dao.insertPromotions(promotions)

        // when
        val morePromotions = listOf(promotionEntity { withId("id3") })
        dao.insertPromotions(morePromotions) // ⚠️ la funcion que testiamos

        // then
        val result = dao.getAllPromotions().first()
        assertEquals("Debe haber tres promociones", 3, result.size)
    }

    @Test
    fun givenPromotions_whenClearPromotions_thenEmitsEmptyPromotions() = runTest {
        // given
        val promotions = listOf(
            promotionEntity { withId("id1") },
            promotionEntity { withId("id2") },
        )
        dao.insertPromotions(promotions)

        // when
        dao.clearPromotions() // ⚠️ la funcion que testiamos

        // then
        val result = dao.getAllPromotions().first()
        assertEquals("Debe estar vacío", 0, result.size)
    }

    @Test
    fun givenPromotions_whenReplaceAll_thenEmitsNewsPromotions() = runTest {
        // given
        val promotions = listOf(
            promotionEntity { withId("id1") },
            promotionEntity { withId("id2") },
        )
        dao.insertPromotions(promotions)

        // when
        val newPromotions = listOf(
            promotionEntity { withId("id1") },
            promotionEntity { withId("id5") },
            promotionEntity { withId("id4") }
        )
        dao.replaceAll(newPromotions) // ⚠️ la funcion que testiamos

        // then
        val result = dao.getAllPromotions().first()
        assertEquals("Debe haber tres promociones", 3, result.size)
        assertTrue("Debe contener id1", result.any { it.id == "id1" })
        assertTrue("Debe contener id4", result.any { it.id == "id4" })
        assertTrue("Debe contener id5", result.any { it.id == "id5" })
        assertTrue("No Debe contener id2", result.none { it.id == "id2" })
    }

}