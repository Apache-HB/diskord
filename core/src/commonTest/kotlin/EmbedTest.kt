import com.serebit.strife.entities.EmbedBuilder
import com.serebit.strife.entities.embed
import com.serebit.strife.entities.footer
import com.serebit.strife.entities.title
import kotlin.test.Test
import kotlin.test.assertFailsWith


class EmbedTest {
    private fun generateFields(length: Int = EmbedBuilder.FIELD_MAX) = MutableList(length) {
        EmbedBuilder.FieldBuilder("$it", "$it", false)
    }

    @Test
    fun `invalid title`() {
        assertFailsWith<IllegalArgumentException> {
            embed { title("") }
        }
        assertFailsWith<IllegalArgumentException> {
            embed { title("X".repeat((EmbedBuilder.TITLE_MAX + 1))) }
        }
    }

    @Test
    fun `invalid description`() {
        assertFailsWith<IllegalArgumentException> {
            embed { title("X"); description = "" }
        }
        assertFailsWith<IllegalArgumentException> {
            embed { title("X"); description = "X".repeat((EmbedBuilder.DESCRIPTION_MAX + 1)) }
        }
    }

    @Test
    fun `too many fields`() {
        assertFailsWith<IllegalStateException> {
            embed {
                title("X")
                description = "X"
                fields.addAll(generateFields(EmbedBuilder.FIELD_MAX + 1))
            }
        }
    }

    @Test
    fun `invalid field name`() {
        assertFailsWith<IllegalArgumentException> { EmbedBuilder.FieldBuilder("", "X") }
        assertFailsWith<IllegalArgumentException> {
            EmbedBuilder.FieldBuilder("X".repeat((EmbedBuilder.FIELD_NAME_MAX + 1)), "X")
        }
    }

    @Test
    fun `invalid field content`() {
        assertFailsWith<IllegalArgumentException> {
            EmbedBuilder.FieldBuilder("X", "")
        }
        assertFailsWith<IllegalArgumentException> {
            EmbedBuilder.FieldBuilder("X", "X".repeat((EmbedBuilder.FIELD_VAL_MAX + 1)))
        }
    }

    @Test
    fun `invalid footer`() {
        assertFailsWith<IllegalArgumentException> {
            embed { footer { text = "" } }
        }
        assertFailsWith<IllegalArgumentException> {
            embed { footer { text = "X".repeat((EmbedBuilder.FOOTER_MAX + 1)) } }
        }
    }

}
