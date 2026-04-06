package com.artemiod.cursotestingandroid.productlist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImp @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val dataStoreFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    override val inStockOnly: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[IN_STOCK_ONLY_KEY] ?: false
    }

    override val selectedCategory: Flow<String?> = dataStoreFlow.map { preferences ->
        preferences[SELECT_CATEGORY_KEY]
    }

    override val filtersVisible: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[FILTER_VISIBLE_KEY] ?: true
    }

    override val themeMode: Flow<ThemeMode> = dataStoreFlow.map { preferences ->
        when (preferences[THEME_MODE_KEY]) {
            1 -> ThemeMode.LIGHT
            2 -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }
    override val sortOption: Flow<SortOption> = dataStoreFlow.map { preferences ->
        val raw = preferences[SORT_OPTION_KEY]
        runCatching {
            SortOption.valueOf(raw ?: SortOption.NONE.name)
        }.getOrDefault(SortOption.NONE)
    }


    override suspend fun setInStockOnly(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[IN_STOCK_ONLY_KEY] = value
        }
    }

    override suspend fun setFiltersVisible(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[FILTER_VISIBLE_KEY] = value
        }
    }

    override suspend fun setSelectedCategory(value: String?) {
        dataStore.edit { preferences ->
            if (value == null) {
                preferences.remove(SELECT_CATEGORY_KEY)
            } else {
                preferences[SELECT_CATEGORY_KEY] = value
            }
        }
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        dataStore.edit { preferences ->
            when (value) {
                ThemeMode.DARK -> preferences[THEME_MODE_KEY] = ThemeMode.DARK.id
                ThemeMode.LIGHT -> preferences[THEME_MODE_KEY] = ThemeMode.LIGHT.id
                ThemeMode.SYSTEM -> preferences[THEME_MODE_KEY] = ThemeMode.SYSTEM.id
            }
        }
    }

    override suspend fun setSortOption(value: SortOption) {
        dataStore.edit { preferences ->
            preferences[SORT_OPTION_KEY] = value.name
        }
    }

    companion object {
        private val IN_STOCK_ONLY_KEY = booleanPreferencesKey("in_stock_only")
        private val FILTER_VISIBLE_KEY = booleanPreferencesKey("filter_visible")
        private val SELECT_CATEGORY_KEY = stringPreferencesKey("select_category")
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        private val SORT_OPTION_KEY = stringPreferencesKey("sort_option")
    }
}