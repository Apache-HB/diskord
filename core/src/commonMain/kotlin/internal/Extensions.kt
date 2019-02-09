package com.serebit.strife.internal

import com.soywiz.klock.DateFormat

internal val DateFormat.Companion.ISO_FORMAT get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
