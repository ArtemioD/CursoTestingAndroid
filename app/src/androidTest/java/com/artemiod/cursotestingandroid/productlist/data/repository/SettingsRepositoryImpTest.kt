package com.artemiod.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImpTest {

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var settingRepository: SettingsRepository

    @Before
    fun setUp() = runTest {
        hilt.inject()
        (settingRepository as? SettingsRepositoryImp)?.clear()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenNoDataSaved_whenInStockOnlyIsRead_thenReturnsDefaultFalse() = runTest {
        val result = settingRepository.inStockOnly.first()
    }

    @Test
    fun givenNoDataSaved_whenFilterVisibleIsRead_thenReturnsDefaultTrue() = runTest {
        val result = settingRepository.filtersVisible.first()
        assertTrue(result)
    }

    @Test
    fun givenNoDataSaved_whenSelectCategoryIsRead_thenReturnsDefaultNull() = runTest {
        val result = settingRepository.selectedCategory.first()
        assertNull(result)
    }

    @Test
    fun givenNoDataSaved_whenThemeModelIsRead_thenReturnsDefaultSystem() = runTest {
        val result = settingRepository.themeMode.first()
        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun givenNoDataSaved_whenSortOptionsIsRead_thenReturnsDefaultNone() = runTest {
        val result = settingRepository.sortOption.first()
        assertEquals(SortOption.NONE, result)
    }

    @Test
    fun givenRepository_whenSetFilterVisibleToFalse_thenPersistValue() = runTest {
        settingRepository.setFiltersVisible(false)
        val result = settingRepository.filtersVisible.first()
        assertTrue(!result)
    }

    @Test
    fun givenMultipleSettingsChanges_whenReadAll_thenStateIsConsistent() = runTest {
        settingRepository.setFiltersVisible(false)
        settingRepository.setInStockOnly(true)
        settingRepository.setSortOption(SortOption.DISCOUNT)
        settingRepository.setThemeMode(ThemeMode.DARK)
        settingRepository.setSelectedCategory("papas")

        val filterVisible = settingRepository.filtersVisible.first()
        val themeMode = settingRepository.themeMode.first()
        val selectedCategory = settingRepository.selectedCategory.first()
        val discount = settingRepository.sortOption.first()
        val inStockOnly = settingRepository.inStockOnly.first()

        assertTrue(!filterVisible)
        assertEquals(ThemeMode.DARK, themeMode)
        assertEquals("papas", selectedCategory)
        assertEquals(SortOption.DISCOUNT, discount)
        assertTrue(inStockOnly)
    }



}