package com.artemiod.cursotestingandroid.core.data.util

import com.artemiod.cursotestingandroid.core.domain.util.Clock
import java.time.Instant
import javax.inject.Inject

class SystemClock @Inject constructor() : Clock {
    override fun now(): Instant = Instant.now()
}