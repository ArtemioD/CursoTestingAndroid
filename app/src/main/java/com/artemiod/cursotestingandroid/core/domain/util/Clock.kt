package com.artemiod.cursotestingandroid.core.domain.util

import java.time.Instant

interface Clock {
    fun now(): Instant
}