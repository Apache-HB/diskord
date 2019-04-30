package com.serebit.strife.internal

import java.lang.ref.WeakReference

internal actual class WeakReference<T> actual constructor(reference: T) : WeakReference<T>(reference)