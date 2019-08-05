package com.serebit.strife.data

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * Represents a color in the sRGB color space. The first 2 places represent red,
 * the second two places represent green, and the last two represent blue.
 *
 *      Color : 0xRRGGBB
 *      black = 0x000000
 *      white = 0xFFFFFF
 *
 * @constructor Composes a new color from the composite of the values of the three color channels (red, green, and
 * blue), where 0xFFFFFF is white and 0x000000 is black.
 *
 * @property rgb The RGB int value, containing each channel's color values at different bit offsets.
 */
@Serializable
data class Color(val rgb: Int) {
    companion object {
        /**
         * Black is the darkest possible color, the result of the absence of color. It is associated with ink, coal,
         * and oil, and is the opposite of [WHITE].
         */
        val BLACK: Color = Color(0x000000)
        /** Dark grey is the color directly between [BLACK] and [GREY]. */
        val DARK_GREY: Color = Color(0x3F3F3F)
        /**
         * Grey (or gray) is the color directly between [BLACK] and [WHITE]. It is associated with storm clouds, ash,
         * and lead.
         */
        val GREY: Color = Color(0x7F7F7F)
        /** Light grey is the color directly between [GREY] and [WHITE]. */
        val LIGHT_GREY: Color = Color(0xBFBFBF)
        /**
         * White is the lightest possible color, and is composed of a combination of red, green, and blue in the RGB
         * color model. It is associated with snow, chalk, and milk, and is the opposite of [BLACK].
         */
        val WHITE: Color = Color(0xFFFFFF)
        /**
         * Red is the first of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [CYAN].
         */
        val RED: Color = Color(0xFF0000)
        /**
         * Orange is the color directly between [RED] and [YELLOW] on the RGB color circle, and is named after the tree
         * bearing fruit of the same name. Its complement in the RGB color model is [AZURE].
         */
        val ORANGE: Color = Color(0xFF7F00)
        /**
         * Yellow is the color directly between [RED] and [GREEN] on the RGB color circle, and is one of the primary
         * colors in the CMYK color model. Its complement in the RGB color model is [BLUE].
         */
        val YELLOW: Color = Color(0xFFFF00)
        /**
         * Chartreuse is the color directly between [YELLOW] and [GREEN] on the RGB color circle. Its complement in
         * the RGB color model is [VIOLET].
         */
        val CHARTREUSE: Color = Color(0x7FFF00)
        /**
         * Green is the second of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [MAGENTA].
         */
        val GREEN: Color = Color(0x00FF00)
        /**
         * Also known as "Spring Green", mint is the color directly between [GREEN] and [CYAN] on the RGB color
         * circle. Its complement in the RGB color model is [ROSE].
         */
        val MINT: Color = Color(0x00FF7F)
        /**
         * Cyan is the color directly between [GREEN] and [BLUE] on the RGB color circle, and is one of the primary
         * colors in the CMYK color space. Its complement in the RGB color model is [RED].
         */
        val CYAN: Color = Color(0x00FFFF)
        /**
         * Often described as the color of the sky on a clear day, azure is the color directly between [CYAN] and [BLUE]
         * on the RGB color circle. Its complement in the RGB color model is [ORANGE].
         */
        val AZURE: Color = Color(0x007FFF)
        /**
         * Blue is the last of the three primary colors in the RGB color model. Its complement in the RGB color model
         * is [YELLOW].
         */
        val BLUE: Color = Color(0x0000FF)
        /**
         * Violet is the color directly between [BLUE] and [MAGENTA] on the RGB color circle. Its complement in the RGB
         * color model is [CHARTREUSE].
         */
        val VIOLET: Color = Color(0x7F00FF)
        /**
         * Magenta is the color directly between [BLUE] and [RED] on the RGB color circle, and is one of the primary
         * colors in the CMYK color space. Its complement in the RGB color model is [GREEN].
         */
        val MAGENTA: Color = Color(0xFF00FF)
        /**
         * Rose is the color directly between [MAGENTA] and [PINK] on the RGB color circle. Its complement in the RGB
         * color model is [MINT].
         */
        val ROSE: Color = Color(0xFF007F)
        /** Pink is a pale red color associated with charm and femininity. */
        val PINK: Color = Color(0xFF7FFF)
        /** Brown is a dark orange color, associated with dirt and wood. */
        val BROWN: Color = Color(0x7F3F00)

        /** "Blurple" is a color named by Discord. Its appearance is somewhere between [BLUE] and [VIOLET]. */
        val BLURPLE: Color = Color(0x7289DA)
        /** "Greyple" is a color named by Discord. Its appearance is close to [GREY], with a slight tint of [BLUE]. */
        val GREYPLE: Color = Color(0x99AAB5)
    }
}

/** The red bits in the color, from `0` to `255` or `0x00` to `0xFF`. */
val Color.red: Int get() = rgb shr 16 and 0xFF

/** The green bits in the color, from `0` to `255` or `0x00` to `0xFF`. */
val Color.green: Int get() = rgb shr 8 and 0xFF

/** The blue bits in the color, from `0` to `255` or `0x00` to `0xFF`. */
val Color.blue: Int get() = rgb and 0xFF

private inline val Color.min get() = minOf(red, green, blue) * (1 / 255.0)
private inline val Color.max get() = maxOf(red, green, blue) * (1 / 255.0)
private const val SQRT_3 = 1.7320508075688772

/**
 * The hue of this color in the HSV color space. This is measured in degrees, from 0 to 359, where red is 0,
 * green is 120, and blue is 240.
 */
val Color.hue: Int
    get() {
        val hueInRadians = atan2(SQRT_3 * (green - blue), 2.0 * red - green - blue)
        val hueInDegrees = (hueInRadians * (180 / PI)).roundToInt()
        return if (hueInDegrees < 0) hueInDegrees + 360 else hueInDegrees
    }

/**
 * The saturation of this color in the HSV color space. This is in the range of 0 to 1, where 0 is gray and 1 is
 * pure color.
 */
val Color.saturation: Double
    get() = if (max > 0) ((max - min) / max * 100).roundToInt() * 0.01 else 0.0

/**
 * The value (or brightness) of this color in the HSV color space. This is in the range of 0 to 1, where 0 is black
 * and 1 is white.
 */
val Color.value: Double
    get() = (maxOf(red, green, blue) * (1 / 2.55)).roundToInt() * 0.01

internal fun Int.toColor() = Color(this)
