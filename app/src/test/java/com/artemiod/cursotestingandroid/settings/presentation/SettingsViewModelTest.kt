package com.artemiod.cursotestingandroid.settings.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.artemiod.cursotestingandroid.core.MainDispatcherRule
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import com.artemiod.cursotestingandroid.core.fakes.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ⚠️ ejemplo con init()
//    @Test
//    fun exampleTestCrash() {
//        val viewModel = SettingsViewModel(FakeSettingsRepository())
//
//        viewModel.setInStockOnly(true)
//
//        assertTrue("Debe tener stock",viewModel.uiState.value.inStockOnly)
//    }


    // en ves de usar este usamos con Turbine ->la libreria que nos permite testear Flow
//    @Test
//    fun secondExample() = runTest(mainDispatcherRule.scheduler) {
//        val settingsRepository = FakeSettingsRepository().apply {
//            setInStockOnly(true)
//        }
//
//        val viewModel = SettingsViewModel(settingsRepository)
//
//        //val job = launch { viewModel.uiState.collect()}
//        backgroundScope.launch { viewModel.uiState.collect() } // para no usar job
//        advanceUntilIdle()
//
//        assertTrue("Debe tener stock", viewModel.uiState.value.inStockOnly)
//        //job.cancel()
//    }

    @Test
    fun `given repository with values when viewModel is initialized then uiState is updated`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val settingsRepository = FakeSettingsRepository().apply {
                setInStockOnly(true)
            }

            // When
            val viewModel = SettingsViewModel(settingsRepository)

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue("Debe tener stock", state.inStockOnly)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given viewmodel when theme mode is changed then uiState and repository are updated`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val settingsRepository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(settingsRepository)

            viewModel.uiState.test {
                awaitItem()
                // When
                viewModel.setThemeMode(ThemeMode.DARK)

                // Then
                val updateState = awaitItem()
                assertEquals("Debe ser tema oscuro", ThemeMode.DARK, updateState.theme)
                assertEquals(
                    "Debe ser tema oscuro",
                    ThemeMode.DARK,
                    settingsRepository.themeMode.first()
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    // ⚠️  lo mismo que arriba pero usando turbineScope
    @Test
    fun `given viewmodel when theme mode is changed then uiState and repository are updated 2`() =
        runTest(mainDispatcherRule.scheduler) {
            turbineScope {
                // Given
                val settingsRepository = FakeSettingsRepository()
                val viewModel = SettingsViewModel(settingsRepository)
                val state = viewModel.uiState.testIn(this)
                state.awaitItem()

                // When
                viewModel.setThemeMode(ThemeMode.DARK)

                // Then
                val updateState = state.awaitItem()
                assertEquals("Debe ser tema oscuro", ThemeMode.DARK, updateState.theme)
                assertEquals(
                    "Debe ser tema oscuro",
                    ThemeMode.DARK,
                    settingsRepository.themeMode.first()
                )
                state.cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given viewmodel when in stock only is changed then uiState and repository are updated`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val settingsRepository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(settingsRepository)

            viewModel.uiState.test {
                awaitItem()
                // When
                viewModel.setInStockOnly(true)

                // Then
                val updateState = awaitItem()
                assertEquals("Debe tener stock", true, updateState.inStockOnly)
                assertEquals("Debe tener stock", true, settingsRepository.inStockOnly.first())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `given viewmodel when repository change externally when uiState update automatically`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val settingsRepository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(settingsRepository)

            viewModel.uiState.test {
                awaitItem()

                // When
                settingsRepository.setInStockOnly(true)

                // Then
                assertTrue("Debe tener stock", viewModel.uiState.value.inStockOnly)
                cancelAndConsumeRemainingEvents()
            }
        }

}