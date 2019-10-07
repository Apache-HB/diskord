import com.serebit.strife.entities.SkinTone
import com.serebit.strife.entities.UnicodeEmoji
import com.serebit.strife.entities.fromUnicode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EmojiTest {
    @Test
    fun `invalid emoji`() {
        assertFailsWith<IllegalArgumentException> { UnicodeEmoji.fromUnicode("NOT_AN_EMOJI") }
    }

    @Test
    fun `non-skin-tone emoji with a skin tone`() {
        assertFailsWith<IllegalArgumentException> {
            UnicodeEmoji.fromUnicode(UnicodeEmoji.Smile.unicode + SkinTone.LIGHT.unicode)
        }
    }

    @Test
    fun `obtained emoji is equal and has the same type`() {
        val emoji = UnicodeEmoji.fromUnicode(UnicodeEmoji.Smirk.unicode)

        assertTrue(emoji is UnicodeEmoji.Smirk && emoji == UnicodeEmoji.Smirk)
    }

    @Test
    fun `obtained emoji with skin tone is equal and has the same skin tone`() {
        val emoji = UnicodeEmoji.Thumbsup(SkinTone.MEDIUM)
        val obtainedEmoji = UnicodeEmoji.fromUnicode(emoji.combinedUnicode)

        assertEquals(emoji, obtainedEmoji)
        assertEquals(emoji.tone, obtainedEmoji.tone)
        assertEquals(emoji.unicode, obtainedEmoji.unicode)
        assertTrue(obtainedEmoji is UnicodeEmoji.Thumbsup)
    }
}