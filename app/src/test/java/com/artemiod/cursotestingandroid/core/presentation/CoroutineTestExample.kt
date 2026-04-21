package com.artemiod.cursotestingandroid.core.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CoroutineTestExample {


    private suspend fun coroutineSum(a: Int, b: Int): Int {
        delay(5000)
        return a + b
    }

    @Test
    fun coroutineSum_returnsCorrectSum() = runTest {
        val result = coroutineSum(2, 2)
        assert(result == 4)
    }

}