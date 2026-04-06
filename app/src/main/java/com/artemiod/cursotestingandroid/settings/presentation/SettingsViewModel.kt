package com.artemiod.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel(), ContractSettings {

    private val _uiState = MutableStateFlow(SettingsUiState())
    override val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        combine(
            settingsRepository.inStockOnly,
            settingsRepository.themeMode
        ) { inStockOnly, theme ->
            _uiState.value = SettingsUiState(
                inStockOnly = inStockOnly,
                theme = theme
            )
        }.launchIn(viewModelScope)
    }

    override fun setInStockOnly(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setInStockOnly(newState)
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
}