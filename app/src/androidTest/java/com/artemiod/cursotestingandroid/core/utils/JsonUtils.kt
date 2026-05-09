package com.artemiod.cursotestingandroid.core.utils

object JsonUtils {

    fun readJson(fileName: String): String {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}

fun String.asAssets(): String = JsonUtils.readJson(this)