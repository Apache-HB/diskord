package com.serebit.strife.data

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
data class Color(val red: Int, val green: Int, val blue: Int) {
    private val max = maxOf(red, green, blue) / 255.0
    private val min = minOf(red, green, blue) / 255.0
    /**
     * The hue of this color in the HSV color space. This is measured in degrees, from 0 to 359, where red is 0,
     * green is 120, and blue is 240.
     */
    val hue: Int
    /**
     * The saturation of this color in the HSV color space. This is in the range of 0 to 1, where 0 is gray and 1 is
     * pure color.
     */
    val saturation: Double = if (max > 0) ((max - min) / max * 100).roundToInt() / 100.0 else 0.0
    /**
     * The value (or brightness) of this color in the HSV color space. This is in the range of 0 to 1, where 0 is black
     * and 1 is white.
     */
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
         * and oil, and is the opposite of [white].
         */
        val black = Color(0x000000)
        /**
         * Dark grey is the color directly between [black] and [grey].
         */
        val darkGrey = Color(0x3F3F3F)
        /**
         * Grey (or gray) is the color directly between [black] and [white]. It is associated with storm clouds, ash,
         * and lead.
         */
        val grey = Color(0x7F7F7F)
        /**
         * Light grey is the color directly between [grey] and [white].
         */
        val lightGrey = Color(0xBFBFBF)
        /**
         * White is the lightest possible color, and is composed of a combination of red, green, and blue in the RGB
         * color model. It is associated with snow, chalk, and milk, and is the opposite of [black].
         */
        val white = Color(0xFFFFFF)
        /**
         * Red is the first of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [cyan].
         */
        val red = Color(0xFF0000)
        /**
         * Orange is the color directly between [red] and [yellow] on the RGB color circle, and is named after the tree
         * bearing fruit of the same name. Its complement in the RGB color model is [azure].
         */
        val orange = Color(0xFF7F00)
        /**
         * Yellow is the color directly between [red] and [green] on the RGB color circle, and is one of the primary
         * colors in the CMYK color model. Its complement in the RGB color model is [blue].
         */
        val yellow = Color(0xFFFF00)
        /**
         * Chartreuse is the color directly between [yellow] and [green] on the RGB color circle. Its complement in
         * the RGB color model is [violet].
         */
        val chartreuse = Color(0x7FFF00)
        /**
         * Green is the second of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [magenta].
         */
        val green = Color(0x00FF00)
        /**
         * Also known as "Spring Green", mint is the color directly between [green] and [cyan] on the RGB color
         * circle. Its complement in the RGB color model is [rose].
         */
        val mint = Color(0x00FF7F)
        /**
         * Cyan is the color directly between [green] and [blue] on the RGB color circle, and is one of the primary
         * colors in the CMYK color space. Its complement in the RGB color model is [red].
         */
        val cyan = Color(0x00FFFF)
        /**
         * Often described as the color of the sky on a clear day, azure is the color directly between [cyan] and [blue]
         * on the RGB color circle. Its complement in the RGB color model is [orange].
         */
        val azure = Color(0x007FFF)
        /**
         * Blue is the last of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [yellow].
         */
        val blue = Color(0x0000FF)
        /**
         * Violet is the color directly between [blue] and [magenta] on the RGB color circle. Its complement in the RGB
         * color model is [chartreuse].
         */
        val violet = Color(0x7F00FF)
        /**
         * Magenta is the color directly between [blue] and [red] on the RGB color circle, and is one of the primary
         * colors in the CMYK color space. Its complement in the RGB color model is [green].
         */
        val magenta = Color(0xFF00FF)
        /**
         * Rose is the color directly between [magenta] and [pink] on the RGB color circle. Its complement in the RGB
         * color model is [mint].
         */
        val rose = Color(0xFF007F)
        /**
         * Pink is a pale red color associated with charm and femininity.
         */
        val pink = Color(0xFF7FFF)
        /**
         * Brown is a dark orange color, associated with dirt and wood.
         */
        val brown = Color(0x7F3F00)
    }
}

fun Int.toColor() = Color(this)
