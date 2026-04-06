package com.artemiod.cursotestingandroid.settings.presentation

import com.artemiod.cursotestingandroid.core.domain.model.ThemeMode

data class SettingsUiState(
    val inStockOnly: Boolean = false,
    val theme: ThemeMode = ThemeMode.SYSTEM,
)
