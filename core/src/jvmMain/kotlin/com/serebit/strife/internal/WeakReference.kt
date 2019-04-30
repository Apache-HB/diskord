package com.serebit.strife.internal

import java.lang.ref.WeakReference

actual class WeakReference<T : Any>(ref: T) : WeakReference<T>(ref)