import com.serebit.strife.entities.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `obtained emoji is equal`() {
        val emoji = UnicodeEmoji.fromUnicode(UnicodeEmoji.Smirk.unicode)

        assertEquals(emoji, UnicodeEmoji.Smirk)
    }

    @Test
    fun `obtained emoji with skin tone is equal and has the same skin tone`() {
        val emoji = UnicodeEmoji.Thumbsup.medium
        val obtainedEmoji = UnicodeEmoji.fromUnicode(emoji.unicode) as UnicodeEmoji.VariantSkinTone

        assertEquals(emoji, obtainedEmoji)
        assertEquals(emoji.skinTone, obtainedEmoji.skinTone)
        assertEquals(emoji.unicode, obtainedEmoji.unicode)
        assertEquals(obtainedEmoji.withoutTone, UnicodeEmoji.Thumbsup)
    }
}
