package com.artemiod.cursotestingandroid.productlist.data.repository

import com.artemiod.cursotestingandroid.core.domain.corroutines.DispatchersProvider
import com.artemiod.cursotestingandroid.productlist.data.local.LocalDataSource
import com.artemiod.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import com.artemiod.cursotestingandroid.productlist.data.mappers.toDomain
import com.artemiod.cursotestingandroid.productlist.data.mappers.toEntity
import com.artemiod.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.artemiod.cursotestingandroid.productlist.domain.model.Promotion
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PromotionRepositoryImp @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val dispatchers: DispatchersProvider,
    private val json: Json
) : PromotionRepository {

    private val refreshScope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val refreshMutex = Mutex()

    override fun getActivePromotions(): Flow<List<Promotion>> {
        return localDataSource.getAllPromotions()
            .map { entities -> entities.mapNotNull { it.toDomain(json) } }
            .onStart {
                refreshScope.launch {
                    if (!refreshMutex.tryLock()) return@launch
                    try {
                        refreshPromotions()
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

    override suspend fun refreshPromotions() {
        withContext(dispatchers.io) {
            val promotions = remoteDataSource.getPromotions().getOrThrow()
            val promotionsEntity: List<PromotionEntity> = promotions.mapNotNull { it.toEntity(json) }
            localDataSource.savePromotions(promotionsEntity)

        }
    }
}