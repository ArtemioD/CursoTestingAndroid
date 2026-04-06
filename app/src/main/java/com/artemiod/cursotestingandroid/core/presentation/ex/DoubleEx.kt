package com.artemiod.cursotestingandroid.core.presentation.ex

import kotlin.math.roundToInt


fun Double.roundTo2Decimals(): Double {
    return (this * 100.0).roundToInt() / 100.0
}