package com.serebit.diskord.data

actual data class Color actual constructor(actual val rgb: Int) {
    actual val red: Int get() = rgb shr 16 and 0xFF
    actual val green: Int get() = rgb shr 8 and 0xFF
    actual val blue: Int get() = rgb and 0xFF
}
