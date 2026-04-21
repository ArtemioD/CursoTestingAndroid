package com.artemiod.cursotestingandroid.productlist.domain.usecase

import com.artemiod.cursotestingandroid.core.builders.product
import com.artemiod.cursotestingandroid.core.builders.promotion
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.artemiod.cursotestingandroid.productlist.domain.model.PromotionType
import org.junit.Assert.*
import org.junit.Test

class GetPromotionForProductTest {

    private val useCase = GetPromotionForProduct()

    @Test
    fun given_no_promotions_when_invoke_then_returns_null() {
        // Given
        val product = product()

        // when
        val response = useCase(product, emptyList())

        // Then
        assertNull(response)
    }

    @Test
    fun given_percent_promotion_when_invoke_then_returns_discounted_price_rounded_to_2_decimals() {
        // Given
        val productId = "product-1"
        val product = product{
            withId(productId)
            withPrice(10.0)
        }
        val promotion = promotion {
            withType(PromotionType.PERCENT)
            withProductIds(listOf(productId))
            withValue(15.0)
        }

        // when
        val response = useCase(product, listOf(promotion))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(8.50, response.discountedPrice,0.001)
        assertEquals(15.0, response.percent,0.001)
    }

    @Test
    fun given_buy_x_pay_y_and_percent_promotion_when_invoke_then_prioritizes_buy_x_pay_y() {
        // Given
        val productId = "product-id"
        val product = product{
            withId(productId)
            withPrice(10.0)
        }
        val promotionPercent = promotion {
            withType(PromotionType.PERCENT)
            withProductIds(listOf(productId))
            withValue(15.0)
        }
        val promotionBuyXPayY = promotion {
            withType(PromotionType.BUY_X_PAY_Y)
            withProductIds(listOf(productId))
            withBayQuantity(3)
            withValue(2.0)
        }

        // when
        val response = useCase(product, listOf(promotionPercent, promotionBuyXPayY))

        // Then
        assertTrue(response is ProductPromotion.BuyXPayY)
        response as ProductPromotion.BuyXPayY
        assertEquals(3, response.buy)
        assertEquals(2, response.pay)
        assertEquals("3x2", response.label)
    }

    @Test
    fun given_multiple_percent_promotions_when_invoke_then_returns_highest_discount() {
        // Given
        val productId = "product-id"
        val product = product{
            withId(productId)
            withPrice(10.0)
        }
        val promotionLow = promotion {
            withType(PromotionType.PERCENT)
            withProductIds(listOf(productId))
            withValue(5.0)
        }
        val promotionHigh = promotion {
            withType(PromotionType.PERCENT)
            withProductIds(listOf(productId))
            withValue(50.0)
        }

        // when
        val response = useCase(product, listOf(promotionLow, promotionHigh))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        assertEquals(50.0, (response as ProductPromotion.Percent).percent,0.001)
    }

    @Test
    fun given_buy_x_pay_y_without_buy_quantity_when_invoke_then_returns_null() {
        // Given
        val productId = "product-id"
        val product = product{
            withId(productId)
            withPrice(10.0)
        }
        val promotionLow = promotion {
            withType(PromotionType.PERCENT)
            withProductIds(listOf(productId))
            withValue(5.0)
        }
        val brokenBuyXPromotion = promotion {
            withType(PromotionType.BUY_X_PAY_Y)
            withProductIds(listOf(productId))
            withBayQuantity(null)
        }

        // when
        val response = useCase(product, listOf(promotionLow, brokenBuyXPromotion))

        // Then
        assertNull(response)
    }

}