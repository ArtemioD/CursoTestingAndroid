package com.artemiod.cursotestingandroid.productlist.domain.repository

import com.artemiod.cursotestingandroid.productlist.domain.model.Promotion
import kotlinx.coroutines.flow.Flow

interface PromotionRepository {

    fun getActivePromotions(): Flow<List<Promotion>>

    suspend fun refreshPromotions()
}