package com.artemiod.cursotestingandroid.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatchers
import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.core.mockwebserver.ProductErrorDispatcher
import com.artemiod.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.artemiod.cursotestingandroid.core.utils.asAssets
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertFailsWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OfflineFirstIntegrationTest {

    companion object {
        const val DEFAULT_PRODUCTS_ASSET = "product_list_default.json"
        const val UPDATED_PRODUCTS_ASSET = "product_list_updated.json"
        const val DEFAULT_PRODUCTS_SIZE = 3
        const val UPDATED_PRODUCTS_SIZE = 1
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var db: MiniMarketDatabase

    @Inject
    lateinit var productRepository: ProductRepository


    @Before
    fun setUp() {
        hilt.inject()
        db.clearAllTables()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenSuccessfulRefresh_whenGetProducts_thenRoomContainsRemoteProducts() = runTest {
        serveProductsFromAsset(DEFAULT_PRODUCTS_ASSET)

        productRepository.refreshProducts()

        val cachedProducts = productRepository.getProducts().first { products ->
            products.size == DEFAULT_PRODUCTS_SIZE
        }

        assertEquals(DEFAULT_PRODUCTS_SIZE, cachedProducts.size)
    }

    @Test
    fun givenEmptyCacheAndFailedRefresh_whenGetProducts_thenEmitsEmptyList() = runTest {
        serverProductsError()

//        val result = runCatching { productRepository.refreshProducts() }
//        assertTrue(result.isFailure)
        // lo mismo pero con assertFailsWith de la libreria kotlin-test
        assertFailsWith<AppError.NetworkError> {
            productRepository.refreshProducts()
        }
        val products = productRepository.getProducts().first { it.isEmpty() }
        assertTrue(products.isEmpty())
    }

    @Test
    fun givenCachedProductsAndFailedRefresh_whenGetProducts_thenReturnsPreviousCache() = runTest {
        serveProductsFromAsset(DEFAULT_PRODUCTS_ASSET)

        productRepository.refreshProducts()

       productRepository.getProducts().first { products ->
            products.size == DEFAULT_PRODUCTS_SIZE
        }

        serverProductsError()
        assertFailsWith<AppError.NetworkError> {
            productRepository.refreshProducts()
        }

        val cachedProducts = productRepository.getProducts().first { products ->
            products.size == DEFAULT_PRODUCTS_SIZE
        }
        assertEquals(DEFAULT_PRODUCTS_SIZE, cachedProducts.size)
    }

    @Test
    fun givenCachedProducts_whenRefreshWithNewPayload_thenContainsOnlyLatestProducts() = runTest {
        serveProductsFromAsset(DEFAULT_PRODUCTS_ASSET)

        productRepository.refreshProducts()

        productRepository.getProducts().first { products ->
            products.size == DEFAULT_PRODUCTS_SIZE
        }

        serveProductsFromAsset(UPDATED_PRODUCTS_ASSET)
        productRepository.refreshProducts()

        val updatedProducts = productRepository.getProducts().first { products ->
            products.size == UPDATED_PRODUCTS_SIZE
        }
        assertEquals(UPDATED_PRODUCTS_SIZE, updatedProducts.size)
        assertEquals("updated-p1", updatedProducts.first().id)
        assertEquals("Pan Integral", updatedProducts.first().name)
    }

    private fun serveProductsFromAsset(assetName: String) {
        mockWebServer.server.dispatcher = MiniMarketApiDispatchers(
            productJson = assetName.asAssets()
        )
    }

    private fun serverProductsError() {
        mockWebServer.server.dispatcher = ProductErrorDispatcher()
    }
}