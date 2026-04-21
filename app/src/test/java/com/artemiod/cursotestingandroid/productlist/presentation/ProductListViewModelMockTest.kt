package com.artemiod.cursotestingandroid.productlist.presentation

import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.fakes.FakeProductRepository
import com.artemiod.cursotestingandroid.core.fakes.FakePromotionRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.artemiod.cursotestingandroid.core.fakes.FakeSystemClock
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import com.artemiod.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import com.artemiod.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProductListViewModelMockTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository = mockk(relaxed = true) {
        every { selectedCategory } returns flowOf(null)
        every { sortOption } returns flowOf(SortOption.NONE)
        every { inStockOnly } returns flowOf(false)
        every { filtersVisible } returns flowOf(true)
    }

    private fun createViewModel(
        fakeProduct: ProductRepository = FakeProductRepository(),
        fakeSettings: FakeSettingsRepository = FakeSettingsRepository(),
        fakePromotion: FakePromotionRepository = FakePromotionRepository(),
        fakeClock: FakeSystemClock = FakeSystemClock()
    ): ProductListViewModel {

        val getProductsUseCase = GetProductsUseCase(
            productRepository = fakeProduct,
            promotionRepository = fakePromotion,
            getPromotionForProduct = GetPromotionForProduct(),
            settingsRepository = fakeSettings,
            clock = fakeClock
        )

        return ProductListViewModel(
            getProductsUseCase = getProductsUseCase,
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun `given category when set category then delegates to settings repository`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val viewModel = createViewModel()
            val category = "pasta"

            // When
            viewModel.setCategory(category)

            // Then
            coVerify(exactly = 1) { settingsRepository.setSelectedCategory(category) }
        }

    @Test
    fun `given sort option when set sort option then delegates to settings repository`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val viewModel = createViewModel()
            val option = SortOption.DISCOUNT

            // When
            viewModel.setSortOption(option)

            // Then
            coVerify(exactly = 1) { settingsRepository.setSortOption(option) }
        }

    @Test
    fun `given filter visible when set filter visible then delegates to settings repository`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val viewModel = createViewModel()
            val option = true

            // When
            viewModel.setFilterVisible(option)

            // Then
            coVerify(exactly = 1) { settingsRepository.setFiltersVisible(option) }
        }

}