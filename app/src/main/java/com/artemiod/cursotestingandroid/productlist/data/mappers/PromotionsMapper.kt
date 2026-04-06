package com.artemiod.cursotestingandroid.productlist.data.mappers

import com.artemiod.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import com.artemiod.cursotestingandroid.productlist.data.remote.response.PromotionResponse
import com.artemiod.cursotestingandroid.productlist.domain.model.Promotion
import com.artemiod.cursotestingandroid.productlist.domain.model.PromotionType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant

fun PromotionEntity.toDomain(json: Json): Promotion? {

    val decodedProductIds = runCatching {
        json.decodeFromString(
            ListSerializer(String.serializer()), string = productIds
        )
    }.getOrNull() //.getOrElse { emptyList() }

    val finalType = runCatching {
        PromotionType.valueOf(type)
    }.getOrNull() //.getOrElse { PromotionType.PERCENT }

    if (decodedProductIds == null && finalType == null) return null

    val finalOfferValue = when (finalType) {
        PromotionType.PERCENT -> percent
        PromotionType.BUY_X_PAY_Y -> payY
        else -> null
    }?.toDouble()

    finalOfferValue ?: return null

    return Promotion(
        id = id,
        productIds = decodedProductIds!!,
        type = finalType!!,
        value = finalOfferValue,
        bayQuantity = buyX,
        startTime = Instant.ofEpochSecond(startAtEpoch),
        endTime = Instant.ofEpochSecond(endAtEpoch)
    )
}

fun PromotionResponse.toEntity(json: Json): PromotionEntity? {

    if (startAtEpoch == null || endAtEpoch == null) return null

    val productIds = listOf(productId)
    val productsIdsJson = json.encodeToString(
        serializer = ListSerializer(String.serializer()),
        value = productIds
    )

    return PromotionEntity(
        id = id,
        productIds = productsIdsJson,
        type = type,
        percent = percent,
        buyX = buyX,
        payY = payY,
        startAtEpoch = startAtEpoch,
        endAtEpoch = endAtEpoch

    )
}