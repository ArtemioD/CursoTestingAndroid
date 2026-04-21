package com.artemiod.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode
import com.artemiod.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel(), ContractSettings {


//    // ⚠️ la mejor opcion es usar StateFlow si Viene de un Flow (DB/DataStore) -> stateIn
    override val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.inStockOnly,
        settingsRepository.themeMode
    ) { inStockOnly, theme ->
        SettingsUiState(inStockOnly, theme)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    // ⚠️ si es algo manual del usuario (Formularios/Eventos) -> MutableStateFlow
//    private val _uiState = MutableStateFlow(SettingsUiState())
//    override val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
//
//    init {
//        loadSettings()
//    }
//
//    private fun loadSettings() {
//        viewModelScope.launch {
//            delay(2000)
//            combine(
//                settingsRepository.inStockOnly,
//                settingsRepository.themeMode
//            ) { inStockOnly, theme ->
//                _uiState.value = SettingsUiState(
//                    inStockOnly = inStockOnly,
//                    theme = theme
//                )
//            }.launchIn(this)
//        }
//    }

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