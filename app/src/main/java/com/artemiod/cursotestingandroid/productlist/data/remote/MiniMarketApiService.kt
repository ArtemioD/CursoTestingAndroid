package com.artemiod.cursotestingandroid.productlist.data.remote

import com.artemiod.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import com.artemiod.cursotestingandroid.productlist.data.remote.response.PromotionsResponse
import retrofit2.http.GET

interface MiniMarketApiService {

    @GET("data/v1/products.json")
    suspend fun getProducts(): ProductsResponse

    @GET("data/v1/promotions.json")
    suspend fun getPromotions(): PromotionsResponse

}