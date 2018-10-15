package com.serebit.diskord.data

/**
 * Represents a color in the sRGB color space, with black being 0x000000 and white being 0xFFFFFF. The first 2 places
 * represent red, the second two places represent green, and the last two places represent blue.
 *
 * @constructor Composes a new [Color] from the composite of the values of the three color channels (red, green, and
 * blue).
 */
data class Color(val rgb: Int) {
    /**
     * The red bits in the color, ranging from 0 to 255, or hex 0x00 to 0xFF.
     */
    val red: Int = rgb shr 16 and 0xFF
    /**
     * The green bits in the color, ranging from 0 to 255, or hex 0x00 to 0xFF.
     */
    val green: Int = rgb shr 8 and 0xFF
    /**
     * The blue bits in the color, ranging from 0 to 255, or hex 0x00 to 0xFF.
     */
    val blue: Int = rgb and 0xFF

    /**
     * Composes a new [Color] from the individual values of the three color channels.
     */
    constructor(red: Int, green: Int, blue: Int) : this(red shl 16 + green shl 8 + blue)

    companion object {
        val black = Color(0x000000)
        val grey = Color(0x808080)
        val white = Color(0xFFFFFF)
        val red = Color(0xFF0000)
        val yellow = Color(0xFFFF00)
        val green = Color(0x00FF00)
        val cyan = Color(0x00FFFF)
        val blue = Color(0x0000FF)
        val magenta = Color(0xFF00FF)
    }
}
