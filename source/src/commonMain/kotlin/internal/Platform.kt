package com.serebit.diskord.internal

expect object Platform {
    val osName: String

    fun exit(code: Int): Nothing

    fun onExit(callback: () -> Unit)
}
