package com.artemiod.cursotestingandroid.productlist.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatchers
import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.artemiod.cursotestingandroid.core.utils.asAssets
import com.artemiod.cursotestingandroid.productlist.data.repository.SettingsRepositoryImp
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductListViewModelIntegrationTest {

    private companion object {
        const val EXPECTED_PRODUCTS_SIZE = 3
        const val DAIRY_CATEGORY = "Lacteos"
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var getProductsUseCase: GetProductsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() = runTest {
        mockWebServer.server.dispatcher = MiniMarketApiDispatchers(
            productJson = "product_list_default.json".asAssets()
        )
        hilt.inject()
        (settingsRepository as? SettingsRepositoryImp)?.clear()

        productRepository.refreshProducts()
        promotionRepository.refreshPromotions()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenSuccessFulApi_whenViewModelLoads_thenShowProducts() = runTest {
        val viewModel = ProductListViewModel(
            getProductsUseCase,
            settingsRepository
        )
        viewModel.uiState.test {
            val result = awaitSuccessMatching { it.products.size == EXPECTED_PRODUCTS_SIZE }
            assertTrue(result.products.isNotEmpty())
            assertTrue(result.products.size == EXPECTED_PRODUCTS_SIZE)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun givenDairyCategorySelect_whenFiltering_thenOnlyDairyProductsAreShown() = runTest {
        // este no es de confianza... aveses falla
        // usar emulardor en ficisos falla  aveses
        val viewModel = ProductListViewModel(
            getProductsUseCase,
            settingsRepository
        )

        viewModel.uiState.test {

            awaitSuccessMatching { it.products.size == EXPECTED_PRODUCTS_SIZE }

            viewModel.setCategory(DAIRY_CATEGORY)

            val result = awaitSuccessMatching { state ->
                state.selectedCategory == DAIRY_CATEGORY &&
                        state.products.isNotEmpty() &&
                        state.products.all { it.product.category == DAIRY_CATEGORY }
            }

            assertTrue("Debe haber dos productos", result.products.size == 2)
            assertTrue(result.products.all { it.product.category == DAIRY_CATEGORY })

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun givenProductsLoaded_whenSortingByPriceAsc_thenListIsCorrectlyOrdered() = runTest {
        val viewModel = ProductListViewModel(
            getProductsUseCase,
            settingsRepository
        )

        viewModel.uiState.test {

            awaitSuccessMatching { it.products.size == EXPECTED_PRODUCTS_SIZE }

            viewModel.setSortOption(SortOption.PRICE_ASC)

            val result = awaitSuccessMatching { state ->
                state.sortOption == SortOption.PRICE_ASC &&
                        state.products.map { it.product.price } == state.products.map { it.product.price }
                    .sorted()
            }

            assertEquals(10.0,result.products.first().product.price)
            assertEquals(listOf(10.0, 15.0, 20.0),result.products.map { it.product.price })

            cancelAndConsumeRemainingEvents()
        }
    }

    private suspend fun ReceiveTurbine<ProductListUIState>.awaitSuccessMatching(
        predicate: (ProductListUIState.Success) -> Boolean
    ): ProductListUIState.Success {
        while (true) {
            when (val item = awaitItem()) {
                is ProductListUIState.Success -> if (predicate(item)) return item
                is ProductListUIState.Error -> error("Unexpected error: ${item.message}")
                is ProductListUIState.Loading -> Unit
            }
        }
    }

}

