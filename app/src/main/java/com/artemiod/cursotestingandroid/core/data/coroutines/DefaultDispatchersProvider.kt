package com.artemiod.cursotestingandroid.core.data.coroutines

import com.artemiod.cursotestingandroid.core.domain.corroutines.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DefaultDispatchersProvider @Inject constructor() : DispatchersProvider {

    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}