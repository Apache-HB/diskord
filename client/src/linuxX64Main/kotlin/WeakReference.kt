package com.serebit.strife.internal

import kotlin.native.ref.WeakReference as NativeWeakReference

internal actual class WeakReference<T : Any> actual constructor(reference: T) {
    private val ref = NativeWeakReference(reference)

    actual fun get(): T? = ref.get()
}
