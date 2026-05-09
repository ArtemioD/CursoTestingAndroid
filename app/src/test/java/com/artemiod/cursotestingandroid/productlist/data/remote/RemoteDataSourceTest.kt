package com.artemiod.cursotestingandroid.productlist.data.remote

import com.artemiod.cursotestingandroid.core.domain.model.AppError
import com.artemiod.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.artemiod.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory


class RemoteDataSourceTest {

    private val server = MockWebServer()
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var json: Json

    @Before
    fun setUp() {
        server.start()
        json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(MiniMarketApiService::class.java)
        remoteDataSource = RemoteDataSource(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `given empty json response, when getProducts, then returns empty list`() = runTest {
        // given
        server.enqueue(MockResponse().setBody("""{"products":[]}""").setResponseCode(200))

        // when
        val result = remoteDataSource.getProducts() // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)
        assertTrue("Debe estar vacío", result.getOrThrow().isEmpty())

    }

    @Test
    fun `given valid json file, when getProducts, then returns mapped dtos`() = runTest {
        // given
        val jsonResource = ClassLoader.getSystemResource("products_success.json").readText()

//        val jsonResource = Thread.currentThread().contextClassLoader
//            ?.getResourceAsStream("products_success.json")    ?.bufferedReader()
//            ?.use { it.readText() }
//            ?: throw IllegalStateException("No se pudo encontrar el archivo en src/test/resources")

        server.enqueue(MockResponse().setBody(jsonResource).setResponseCode(200))

        // when
        val result = remoteDataSource.getProducts() // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)
        val products = result.getOrThrow()
        assertTrue("No Debe estar vacío", products.isNotEmpty())
        assertEquals("Debe tener 40 productos", 40, products.size)
    }

    @Test
    fun `given serialized products, when getPromotions, then returns mapped dtos`() = runTest {
        // given
        val productResponse = ProductResponse(
            id = "p1",
            name = "Pan",
            priceCents = 100,
            category = "bread",
            stock = 5
        )
        val jsonString = json.encodeToString(ProductsResponse(listOf(productResponse)))
        server.enqueue(MockResponse().setBody(jsonString).setResponseCode(200))

        // when
        val result = remoteDataSource.getProducts() // ⚠️ la funcion que testiamos
        val products = result.getOrThrow()

        // then
        assertTrue("Debe ser exitoso", result.isSuccess)
        assertTrue("No Debe estar vacío", products.isNotEmpty())
        assertEquals("Debe ser #p1", "p1", products.first().id)
        assertEquals("Debe tener 1 producto", 1, products.size)
    }

    @Test
    fun `given 404 response, when getProducts, then returns NotFountError`() = runTest {
        // given
        server.enqueue(MockResponse().setResponseCode(404))

        // when
        val result = remoteDataSource.getProducts() // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser fallido", result.isFailure)
        assertEquals("Debe ser NotFountError", AppError.NotFoundError, result.exceptionOrNull())
    }

    @Test
    fun `given malformed json, when getProducts, then returns UnknownError`() = runTest {
        // given
        server.enqueue(MockResponse().setBody("error_malformed_json").setResponseCode(200))

        // when
        val result = remoteDataSource.getProducts() // ⚠️ la funcion que testiamos

        // then
        assertTrue("Debe ser fallido", result.isFailure)
        assertTrue("Debe ser UnknownError", result.exceptionOrNull() is AppError.UnknownError)
    }

    @Test
    fun `given promotions request, when getPromotions, then calls correct endpoint`() = runTest {
        // given
        server.enqueue(MockResponse().setBody("""{"products":[]}""").setResponseCode(200))

        // when
        remoteDataSource.getPromotions() // ⚠️ la funcion que testiamos
        val request = server.takeRequest()

        // then
        assertEquals("Debe ser GET", "GET", request.method)
        assertEquals("Debe ser /data/v1/products.json", "/data/v1/promotions.json", request.path)
    }

}