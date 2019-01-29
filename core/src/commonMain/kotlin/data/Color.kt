package com.serebit.strife.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
@Serializable
data class Color(val red: Int, val green: Int, val blue: Int) {
    @Transient
    private val max = maxOf(red, green, blue) / 255.0
    @Transient
    private val min = minOf(red, green, blue) / 255.0
    /**
     * The hue of this color in the HSV color space. This is measured in degrees, from 0 to 359, where red is 0,
     * green is 120, and blue is 240.
     */
    @Transient
    val hue: Int
    /**
     * The saturation of this color in the HSV color space. This is in the range of 0 to 1, where 0 is gray and 1 is
     * pure color.
     */
    @Transient
    val saturation: Double = if (max > 0) ((max - min) / max * 100).roundToInt() / 100.0 else 0.0
    /**
     * The value (or brightness) of this color in the HSV color space. This is in the range of 0 to 1, where 0 is black
     * and 1 is white.
     */
    @Transient
    val value: Double = (max * 100).roundToInt() / 100.0

    init {
        val hueInRadians = atan2(sqrt(3.0) * (green - blue), 2.0 * red - green - blue)
        val hueInDegrees = (hueInRadians * (180 / PI)).roundToInt()
        hue = if (hueInDegrees < 0) hueInDegrees + 360 else hueInDegrees
    }

    /**
     * Composes a new [Color] from the composite of the values of the three color channels (red, green, and
     * blue), where 0xFFFFFF is white and 0x000000 is black.
     */
    constructor(rgb: Int) : this(rgb shr 16 and 0xFF, rgb shr 8 and 0xFF, rgb and 0xFF)

    companion object {
        /**
         * Black is the darkest possible color, the result of the absence of color. It is associated with ink, coal,
         * and oil, and is the opposite of [WHITE].
         */
        val BLACK = Color(0x000000)
        /**
         * Dark grey is the color directly between [BLACK] and [GREY].
         */
        val DARK_GREY = Color(0x3F3F3F)
        /**
         * Grey (or gray) is the color directly between [BLACK] and [WHITE]. It is associated with storm clouds, ash,
         * and lead.
         */
        val GREY = Color(0x7F7F7F)
        /**
         * Light grey is the color directly between [GREY] and [WHITE].
         */
        val LIGHT_GREY = Color(0xBFBFBF)
        /**
         * White is the lightest possible color, and is composed of a combination of red, green, and blue in the RGB
         * color model. It is associated with snow, chalk, and milk, and is the opposite of [BLACK].
         */
        val WHITE = Color(0xFFFFFF)
        /**
         * Red is the first of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [CYAN].
         */
        val RED = Color(0xFF0000)
        /**
         * Orange is the color directly between [RED] and [YELLOW] on the RGB color circle, and is named after the tree
         * bearing fruit of the same name. Its complement in the RGB color model is [AZURE].
         */
        val ORANGE = Color(0xFF7F00)
        /**
         * Yellow is the color directly between [RED] and [GREEN] on the RGB color circle, and is one of the primary
         * colors in the CMYK color model. Its complement in the RGB color model is [BLUE].
         */
        val YELLOW = Color(0xFFFF00)
        /**
         * Chartreuse is the color directly between [YELLOW] and [GREEN] on the RGB color circle. Its complement in
         * the RGB color model is [VIOLET].
         */
        val CHARTREUSE = Color(0x7FFF00)
        /**
         * Green is the second of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [MAGENTA].
         */
        val GREEN = Color(0x00FF00)
        /**
         * Also known as "Spring Green", mint is the color directly between [GREEN] and [CYAN] on the RGB color
         * circle. Its complement in the RGB color model is [ROSE].
         */
        val MINT = Color(0x00FF7F)
        /**
         * Cyan is the color directly between [GREEN] and [BLUE] on the RGB color circle, and is one of the primary
         * colors in the CMYK color space. Its complement in the RGB color model is [RED].
         */
        val CYAN = Color(0x00FFFF)
        /**
         * Often described as the color of the sky on a clear day, azure is the color directly between [CYAN] and [BLUE]
         * on the RGB color circle. Its complement in the RGB color model is [ORANGE].
         */
        val AZURE = Color(0x007FFF)
        /**
         * Blue is the last of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [YELLOW].
         */
        val BLUE = Color(0x0000FF)
        /**
         * Violet is the color directly between [BLUE] and [MAGENTA] on the RGB color circle. Its complement in the RGB
         * color model is [CHARTREUSE].
         */
        val VIOLET = Color(0x7F00FF)
        /**
         * Magenta is the color directly between [BLUE] and [RED] on the RGB color circle, and is one of the primary
         * colors in the CMYK color space. Its complement in the RGB color model is [GREEN].
         */
        val MAGENTA = Color(0xFF00FF)
        /**
         * Rose is the color directly between [MAGENTA] and [PINK] on the RGB color circle. Its complement in the RGB
         * color model is [MINT].
         */
        val ROSE = Color(0xFF007F)
        /**
         * Pink is a pale red color associated with charm and femininity.
         */
        val PINK = Color(0xFF7FFF)
        /**
         * Brown is a dark orange color, associated with dirt and wood.
         */
        val BROWN = Color(0x7F3F00)
    }
}

internal fun Int.toColor() = Color(this)
