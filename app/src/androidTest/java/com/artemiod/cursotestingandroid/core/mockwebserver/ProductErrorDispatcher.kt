package com.artemiod.cursotestingandroid.core.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class ProductErrorDispatcher : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        return when {

            request.path?.contains("products.json") == true ->
                MockResponse().setResponseCode(500)

            else -> MockResponse().setResponseCode(404)
        }
    }
}