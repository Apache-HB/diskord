package com.serebit.diskord

import com.serebit.loggerkt.LogLevel
import com.serebit.loggerkt.Logger

fun main(args: Array<String>) {
    Logger.level = LogLevel.TRACE
    diskord(args[0])
}
