package com.artemiod.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImpTest {

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        hilt.inject()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    private val productJson = """
        {"products": [
            {"id": "p1", "name": "Pan", "description": "Pan fresco", "category": "Comida", "priceCents": 150, "stock": 10},
            {"id": "p2", "name": "Leche", "description": "Leche entera", "category": "Lacteos", "priceCents": 200, "stock": 5}
        ]}
    """.trimIndent()

    @Test
    fun givenValidProductsJson_whenRefreshIsCalled_thenDatabaseEmitsProductsFromRoom() = runTest {
        // given
        mockWebServer.server.enqueue(MockResponse().setBody(productJson).setResponseCode(200))

        // when
        productRepository.refreshProducts()

        // then
        val products = productRepository.getProducts().first()
        assertTrue("No debe estar vacío", products.isNotEmpty())
        assertEquals("Debe haber dos productos",2, products.size)
        assertEquals("El nombre del producto debe ser Pan","Pan", products.find { it.id == "p1" }?.name)
    }

    @Test
    fun givenEmptyProductsJson_whenRefreshIsCalled_thenGetProductsEmitsEmptyList() = runTest {
        // given
        mockWebServer.server.enqueue(MockResponse().setBody("""{"products": []}""").setResponseCode(200))

        // when
        productRepository.refreshProducts()

        // then
        val products = productRepository.getProducts().first()
        assertTrue("Debe estar vacío", products.isEmpty())
    }

    @Test
    fun givenProductJson_whenRefreshAndGetProductById_thenReturnsCorrectProduct() = runTest {
        // given
        mockWebServer.server.enqueue(MockResponse().setBody(productJson).setResponseCode(200))

        // when
        productRepository.refreshProducts()

        // then
        val product = productRepository.getProductById("p1").first()
        assertNotNull("Debe ser un producto, no null",product)
        assertEquals("Debe ser Pan", "Pan",product?.name)
    }

    @Test(expected = Exception::class)
    fun givenServerReturns500_whenRefreshIsCalled_thenItThrowsException() = runTest {
        // given
        mockWebServer.server.enqueue(MockResponse().setResponseCode(500))

        productRepository.refreshProducts()
    }

    @Test
    fun givenCachedProducts_whenRefreshWithNewProducts_thenFlowEmitsUpdatedData() = runTest {
        // given
        mockWebServer.server.enqueue(MockResponse().setBody(productJson).setResponseCode(200))
        productRepository.refreshProducts()

        val productJsonUpdate = """
        {"products": [
            {"id": "p1", "name": "Pan Integral", "description": "Pan fresco", "category": "Comida", "priceCents": 450, "stock": 10}
        ]}
    """.trimIndent()

        mockWebServer.server.enqueue(MockResponse().setBody(productJsonUpdate).setResponseCode(200))
        productRepository.refreshProducts()

        val products = productRepository.getProducts().first()

        assertEquals("Debe ser Pan Integral","Pan Integral",products.find { it.id == "p1" }?.name)
        assertEquals("Debe valer $450",4.5,products.find { it.id == "p1" }?.price)
    }

    @Test
    fun givenProductsEndpoint_whenRefreshIsCalled_thenRequestIsGetToCorrectPath() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody(productJson).setResponseCode(200))
        productRepository.refreshProducts()

        val request = mockWebServer.server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("data/v1/products.json") == true)
    }

}