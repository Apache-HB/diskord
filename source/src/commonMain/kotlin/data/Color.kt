package com.serebit.strife.data

/**
 * Represents a color in the sRGB color space, with black being 0x000000 and white being 0xFFFFFF. The first 2 places
 * represent red, the second two places represent green, and the last two places represent blue.
 *
 * @constructor Composes a new [Color] from the individual values of the three color channels.
 *
 * @property red The red bits in the color, ranging from 0 to 255, or hex 0x00 to 0xFF.
 * @property green The green bits in the color, ranging from 0 to 255, or hex 0x00 to 0xFF.
 * @property blue The blue bits in the color, ranging from 0 to 255, or hex 0x00 to 0xFF.
 */
data class Color(val red: Int, val green: Int, val blue: Int) {
    /**
     * Composes a new [Color] from the composite of the values of the three color channels (red, green, and
     * blue).
     */
    constructor(rgb: Int) : this(rgb shr 16 and 0xFF, rgb shr 8 and 0xFF, rgb and 0xFF)

    companion object {
        val black = Color(0x000000)
        val darkGrey = Color(0x404040)
        val grey = Color(0x808080)
        val lightGrey = Color(0xC0C0C0)
        val white = Color(0xFFFFFF)
        val red = Color(0xFF0000)
        val orange = Color(0xFF8000)
        val yellow = Color(0xFFFF00)
        val lime = Color(0x80FF00)
        val green = Color(0x00FF00)
        val marine = Color(0x00FF89)
        val cyan = Color(0x00FFFF)
        val sky = Color(0x0080FF)
        val blue = Color(0x0000FF)
        val purple = Color(0x8000FF)
        val magenta = Color(0xFF00FF)
    }
}

fun Int.toColor() = Color(this)
