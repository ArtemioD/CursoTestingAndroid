package com.artemiod.cursotestingandroid

import app.cash.turbine.test
import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import com.artemiod.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakeSettings: SettingsRepository = FakeSettingsRepository()
    ) = MainViewModel(fakeSettings)

    @Test
    fun `given repository with dark mode when initialized then emits dark theme mode`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val fakeSettings = FakeSettingsRepository().apply { setThemeMode(ThemeMode.DARK) }
            val viewModel = createViewModel(fakeSettings)

            viewModel.themeMode.test {
                // When
                val state = awaitItem()

                // Then
                assertEquals("Debe ser dark", ThemeMode.DARK, state)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given default repository when initialized then emits system theme mode`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val viewModel = createViewModel()

            viewModel.themeMode.test {
                // When
                val state = awaitItem()

                // Then
                assertEquals("Debe ser SYSTEM", ThemeMode.SYSTEM, state)
                cancelAndConsumeRemainingEvents()
            }
        }
}