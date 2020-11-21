package com.serebit.strife.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal actual fun newSingleThreadContext(name: String): CoroutineDispatcher =
    kotlinx.coroutines.newSingleThreadContext(name)