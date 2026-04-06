package com.artemiod.cursotestingandroid.productlist.data.repository

import com.artemiod.cursotestingandroid.core.domain.corroutines.DispatchersProvider
import com.artemiod.cursotestingandroid.productlist.data.local.LocalDataSource
import com.artemiod.cursotestingandroid.productlist.data.mappers.toDomain
import com.artemiod.cursotestingandroid.productlist.data.mappers.toEntity
import com.artemiod.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.artemiod.cursotestingandroid.productlist.domain.model.Product
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepositoryImp @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val dispatchers: DispatchersProvider
) : ProductRepository {

    private val refreshScope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val refreshMutex = Mutex()

    override fun getProducts(): Flow<List<Product>> {
        return localDataSource.getAllProducts()
            .map { entities -> entities.mapNotNull { it.toDomain() } }
            .onStart {
                refreshScope.launch {
                    if (!refreshMutex.tryLock()) return@launch
                    try {
                        refreshProducts()
                    } catch (e: Exception) {
                        // error
                    } finally {
                        refreshMutex.unlock()
                    }
                }
            }
            .catch {
                // Log importante
            }
    }

    override fun getProductById(id: String): Flow<Product?> {
        return localDataSource.getProductById(id)
            .map { it?.toDomain() }
            .catch { e: Throwable ->
                // Log importante
                // analytic.trackError(e)
            }
    }

    override fun getProductsByIds(ids: Set<String>): Flow<List<Product>> {
        return localDataSource.getProductsByIds(ids)
            .map { entities -> entities.mapNotNull { it.toDomain() } }
    }

    override suspend fun refreshProducts() {
        withContext(dispatchers.io) {
            val products = remoteDataSource.getProducts().getOrThrow()
            val productsEntity = products.map { it.toEntity() }
            localDataSource.saveProducts(productsEntity)
        }

    }
}