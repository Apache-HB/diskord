package com.serebit.strife.internal

import com.soywiz.klock.DateFormat

/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX */
internal val DateFormat.Companion.ISO_WITH_MS get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX")
/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ssXX */
internal val DateFormat.Companion.ISO_WITHOUT_MS get() = DateFormat("yyyy-MM-dd'T'HH:mm:ssXX")
