import com.serebit.strife.data.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorTests {
    @Test
    fun `hue calculation`() {
        assertEquals(Color.RED.hue, 0)
        assertEquals(Color.YELLOW.hue, 60)
        assertEquals(Color.GREEN.hue, 120)
        assertEquals(Color.CYAN.hue, 180)
        assertEquals(Color.BLUE.hue, 240)
        assertEquals(Color.MAGENTA.hue, 300)
    }

    @Test
    fun `saturation calculation`() {
        assertEquals(Color.RED.saturation, 1.0)
        assertEquals(Color(0xbf6060).saturation, 0.5)
        assertEquals(Color.GREY.saturation, 0.0)
    }

    @Test
    fun `value calculation`() {
        assertEquals(Color.BLACK.value, 0.0)
        assertEquals(Color.DARK_GREY.value, 0.25)
        assertEquals(Color.GREY.value, 0.5)
        assertEquals(Color.LIGHT_GREY.value, 0.75)
        assertEquals(Color.WHITE.value, 1.0)
    }
}
