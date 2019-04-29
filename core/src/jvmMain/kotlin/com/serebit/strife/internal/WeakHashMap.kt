package com.serebit.strife.internal

import java.util.WeakHashMap

internal actual class WeakHashMap<K, V> actual constructor() : MutableMap<K, V>, WeakHashMap<K, V>()
