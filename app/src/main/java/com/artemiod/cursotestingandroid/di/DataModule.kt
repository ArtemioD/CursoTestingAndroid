package com.artemiod.cursotestingandroid.di

import com.artemiod.cursotestingandroid.cart.data.repository.CartItemRepositoryImp
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.artemiod.cursotestingandroid.core.domain.corroutines.DispatchersProvider
import com.artemiod.cursotestingandroid.productlist.data.repository.ProductRepositoryImp
import com.artemiod.cursotestingandroid.productlist.data.repository.PromotionRepositoryImp
import com.artemiod.cursotestingandroid.productlist.data.repository.SettingsRepositoryImp
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDispatchersProvider(defaultDispatchersProvider: DefaultDispatchersProvider): DispatchersProvider {
        return defaultDispatchersProvider
    }

    @Provides
    @Singleton
    fun provideProductRepository(productRepositoryImp: ProductRepositoryImp): ProductRepository {
        return productRepositoryImp
    }

    @Provides
    @Singleton
    fun providePromotionsRepository(promotionRepositoryImp: PromotionRepositoryImp): PromotionRepository {
        return promotionRepositoryImp
    }

    @Provides
    @Singleton
    fun provideSettingRepository(settingsRepositoryImp: SettingsRepositoryImp): SettingsRepository {
        return settingsRepositoryImp
    }

    @Provides
    @Singleton
    fun provideCartRepository(cartRepositoryImp: CartItemRepositoryImp): CartItemRepository {
        return cartRepositoryImp
    }
}
