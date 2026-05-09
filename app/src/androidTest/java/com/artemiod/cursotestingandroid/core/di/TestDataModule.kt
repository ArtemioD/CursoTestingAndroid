package com.artemiod.cursotestingandroid.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.artemiod.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.artemiod.cursotestingandroid.cart.data.repository.CartItemRepositoryImp
import com.artemiod.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.artemiod.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.artemiod.cursotestingandroid.core.data.util.SystemClock
import com.artemiod.cursotestingandroid.core.domain.corroutines.DispatchersProvider
import com.artemiod.cursotestingandroid.core.domain.util.Clock
import com.artemiod.cursotestingandroid.di.DataModule
import com.artemiod.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.artemiod.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.artemiod.cursotestingandroid.productlist.data.repository.ProductRepositoryImp
import com.artemiod.cursotestingandroid.productlist.data.repository.PromotionRepositoryImp
import com.artemiod.cursotestingandroid.productlist.data.repository.SettingsRepositoryImp
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.UUID
import javax.inject.Singleton

private val Context.testingDataStore by preferencesDataStore("testing_settings")

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
object TestDataModule {

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
    fun provideProductDao(database: MiniMarketDatabase): ProductDao = database.productDao()

    @Provides
    fun providePromotionDao(database: MiniMarketDatabase): PromotionDao = database.promotionDao()

    @Provides
    fun provideCartItemDao(database: MiniMarketDatabase): CartItemDao = database.cartItemDao()

    @Provides
    @Singleton
    fun provideDatabase(): MiniMarketDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, MiniMarketDatabase::class.java)
            .build()
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

    @Provides
    @Singleton
    fun provideClock(systemClock: SystemClock): Clock {
        return systemClock
    }

    @Provides
    @Singleton
    fun provideDataStore() : DataStore<Preferences> {
        //return ApplicationProvider.getApplicationContext<Context>().testingDataStore
        val context = ApplicationProvider.getApplicationContext<Context>()

        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("test_settings_${UUID.randomUUID()}")
            },
        )
    }

}