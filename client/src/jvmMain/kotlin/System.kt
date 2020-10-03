package com.serebit.strife.internal

import java.io.PrintWriter
import java.io.StringWriter

internal actual val osName: String get() = System.getProperty("os.name")

internal actual val Throwable.stackTraceAsString: String
    get() = StringWriter().also { PrintWriter(it).use(::printStackTrace) }.toString()
