package com.serebit.strife

import java.util.WeakHashMap

internal actual class WeakHashMap<K, V> : MutableMap<K, V>, WeakHashMap<K, V>()