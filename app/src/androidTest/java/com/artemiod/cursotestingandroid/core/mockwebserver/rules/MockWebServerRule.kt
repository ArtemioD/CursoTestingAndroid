package com.artemiod.cursotestingandroid.core.mockwebserver.rules

import com.artemiod.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockWebServerRule : TestWatcher() {
    val server = MockWebServer()

    override fun starting(description: Description?) {
        super.starting(description)
        server.start()

        MockWebServerUrlHolder.baseUrl = server.url("/").toString()
        // Forzar 127.0.0.1 en lugar de localhost
//        val url = server.url("/").newBuilder().host("127.0.0.1").build().toString()
//        MockWebServerUrlHolder.baseUrl = url
//
//        Thread.sleep(500) // Pausa de medio segundo para estabilidad en hardware real
    }

    override fun finished(description: Description?) {
        server.shutdown()
        super.finished(description)
    }
}