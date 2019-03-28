import com.serebit.strife.entities.EmbedBuilder
import com.serebit.strife.entities.embed
import com.serebit.strife.entities.footer
import com.serebit.strife.entities.title
import com.serebit.strife.internal.times
import kotlin.test.Test
import kotlin.test.assertFailsWith


class EmbedTest {
    private fun generateFields(length: Int = EmbedBuilder.FIELD_MAX) = MutableList(length) {
        EmbedBuilder.FieldBuilder(false).apply {
            name = "$it"; content = "$it"
        }
    }

    @Test
    fun `invalid title`() {
        assertFailsWith<IllegalArgumentException> {
            embed { title("") }
        }
        assertFailsWith<IllegalArgumentException> {
            embed { title("X" * (EmbedBuilder.TITLE_MAX + 1)) }
        }
    }

    @Test
    fun `invalid description`() {
        assertFailsWith<IllegalArgumentException> {
            embed { title("X"); description = "" }
        }
        assertFailsWith<IllegalArgumentException> {
            embed { title("X"); description = "X" * (EmbedBuilder.DESCRIPTION_MAX + 1) }
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
        assertFailsWith<IllegalArgumentException> { EmbedBuilder.FieldBuilder().apply { name = ""; content = "X" } }
        assertFailsWith<IllegalArgumentException> {
            EmbedBuilder.FieldBuilder().apply {
                name = "X" * (EmbedBuilder.FIELD_NAME_MAX + 1); content = "X"
            }
        }
    }

    @Test
    fun `invalid field content`() {
        assertFailsWith<IllegalArgumentException> {
            EmbedBuilder.FieldBuilder().apply { name = "X"; content = "" }
        }
        assertFailsWith<IllegalArgumentException> {
            EmbedBuilder.FieldBuilder().apply {
                name = "X"; content = "X" * (EmbedBuilder.FIELD_VAL_MAX + 1)
            }
        }
    }

    @Test
    fun `invalid footer`() {
        assertFailsWith<IllegalArgumentException> {
            embed { footer { text = "" } }
        }
        assertFailsWith<IllegalArgumentException> {
            embed { footer { text = "X" * (EmbedBuilder.FOOTER_MAX + 1) } }
        }
    }

}
