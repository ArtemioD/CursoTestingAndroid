package com.artemiod.cursotestingandroid.core.di

import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.artemiod.cursotestingandroid.di.NetworkModule
import com.artemiod.cursotestingandroid.productlist.data.remote.MiniMarketApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object TestNetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

//    @Provides
//    @Singleton
//    fun provideOkHttpClient(): OkHttpClient {
//        return OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
//            .addInterceptor { chain ->
//                val originalRequest = chain.request()
//                val newBaseUrl = MockWebServerUrlHolder.baseUrl.toHttpUrl()
//
//                val newUrl = originalRequest.url.newBuilder()
//                    .scheme(newBaseUrl.scheme)
//                    .host(newBaseUrl.host)
//                    .port(newBaseUrl.port)
//                    .build()
//
//                val newRequest = originalRequest.newBuilder()
//                    .url(newUrl)
//                    .build()
//
//                chain.proceed(newRequest)
//            }
//            .build()
//    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(MockWebServerUrlHolder.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

//    @Provides
//    @Singleton
//    fun provideRetrofit(
//        okHttpClient: OkHttpClient,
//        json: Json,
//    ): Retrofit {
//        val contentType = "application/json".toMediaType()
//        return Retrofit.Builder()
//            .baseUrl("http://127.0.0.1/")
//            .client(okHttpClient)
//            .addConverterFactory(json.asConverterFactory(contentType))
//            .build()
//    }

    @Provides
    @Singleton
    fun provideMiniMarketApiService(retrofit: Retrofit): MiniMarketApiService {
        return retrofit.create(MiniMarketApiService::class.java)
    }
}