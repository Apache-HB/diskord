import com.serebit.strife.data.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorTests {
    @Test
    fun `hue calculation`() {
        assertEquals(Color.red.hue, 0)
        assertEquals(Color.yellow.hue, 60)
        assertEquals(Color.green.hue, 120)
        assertEquals(Color.cyan.hue, 180)
        assertEquals(Color.blue.hue, 240)
        assertEquals(Color.magenta.hue, 300)
    }

    @Test
    fun `saturation calculation`() {
        assertEquals(Color.red.saturation, 1.0)
        assertEquals(Color(0xbf6060).saturation, 0.5)
        assertEquals(Color.grey.saturation, 0.0)
    }

    @Test
    fun `value calculation`() {
        assertEquals(Color.black.value, 0.0)
        assertEquals(Color.darkGrey.value, 0.25)
        assertEquals(Color.grey.value, 0.5)
        assertEquals(Color.lightGrey.value, 0.75)
        assertEquals(Color.white.value, 1.0)
    }
}
