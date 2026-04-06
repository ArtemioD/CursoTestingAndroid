package com.artemiod.cursotestingandroid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.artemiod.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.artemiod.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.artemiod.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.artemiod.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


private val Context.dataStore by preferencesDataStore("settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideProductDao(database: MiniMarketDatabase): ProductDao = database.productDao()

    @Provides
    fun providePromotionDao(database: MiniMarketDatabase): PromotionDao = database.promotionDao()

    @Provides
    fun provideCartItemDao(database: MiniMarketDatabase): CartItemDao = database.cartItemDao()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MiniMarketDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = MiniMarketDatabase::class.java,
            name = "minimarket_database").build()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) : DataStore<Preferences> {
        return context.dataStore
    }
}