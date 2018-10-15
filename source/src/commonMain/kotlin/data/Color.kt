package com.serebit.diskord.data

data class Color(val rgb: Int) {
    val red: Int get() = rgb shr 16 and 0xFF
    val green: Int get() = rgb shr 8 and 0xFF
    val blue: Int get() = rgb and 0xFF
}
