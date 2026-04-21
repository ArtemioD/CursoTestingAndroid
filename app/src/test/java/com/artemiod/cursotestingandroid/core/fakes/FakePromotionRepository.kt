package com.artemiod.cursotestingandroid.core.fakes

import com.artemiod.cursotestingandroid.productlist.domain.model.Promotion
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePromotionRepository : PromotionRepository {

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())

    fun setPromotions(promotions: List<Promotion>) {
        _promotions.value = promotions
    }

    override fun getActivePromotions(): Flow<List<Promotion>> {
        return _promotions
    }

    override suspend fun refreshPromotions() {}
}