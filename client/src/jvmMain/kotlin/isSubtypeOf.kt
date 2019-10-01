package com.serebit.strife.internal

import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf

internal actual fun KType.isSubtypeOf(other: KType): Boolean = isSubtypeOf(other)
