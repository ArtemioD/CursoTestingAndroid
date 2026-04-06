package com.artemiod.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


interface ContractSettings {
    val uiState: StateFlow<SettingsUiState>
    fun setInStockOnly(newState: Boolean)
    fun setThemeMode(mode: ThemeMode)
}

class FakeSettingsViewModel : ViewModel(), ContractSettings {
    override val uiState: StateFlow<SettingsUiState> =  MutableStateFlow(SettingsUiState())
    override fun setInStockOnly(newState: Boolean) {}
    override fun setThemeMode(mode: ThemeMode) {}
}

