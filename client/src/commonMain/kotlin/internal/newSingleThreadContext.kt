package com.serebit.strife.internal

import kotlinx.coroutines.CoroutineDispatcher

internal expect fun newSingleThreadContext(name: String): CoroutineDispatcher