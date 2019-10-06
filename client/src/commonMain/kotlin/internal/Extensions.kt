package com.serebit.strife.internal

import com.soywiz.klock.DateFormat
import kotlin.reflect.KType

/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX */
internal val DateFormat.Companion.ISO get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]XX").withOptional()

internal expect fun KType.isSubtypeOf(other: KType): Boolean
