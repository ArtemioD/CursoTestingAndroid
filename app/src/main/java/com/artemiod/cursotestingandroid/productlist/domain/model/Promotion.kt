package com.artemiod.cursotestingandroid.productlist.domain.model

import kotlin.time.ExperimentalTime
import java.time.Instant

enum class PromotionType {
    PERCENT,
    BUY_X_PAY_Y
}

@OptIn(ExperimentalTime::class)
data class Promotion(
    val id: String,
    val type: PromotionType,
    val productIds: List<String>,
    val value: Double,
    val bayQuantity: Int? = null,
    val startTime: Instant,
    val endTime: Instant
)
