package com.serebit.strife.internal

import java.util.*

internal actual class WeakCache<K, V> actual constructor() : MutableMap<K, V>, WeakHashMap<K, V>()
