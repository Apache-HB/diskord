package com.serebit.diskord.internal

expect object Platform {
    fun exit(code: Int): Nothing

    fun onExit(callback: () -> Unit)
}
