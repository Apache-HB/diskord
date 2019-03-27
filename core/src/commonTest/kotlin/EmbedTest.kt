import com.serebit.strife.entities.Embed
import com.serebit.strife.entities.Embed.Companion.DESCRIPTION_MAX
import com.serebit.strife.entities.Embed.Companion.FIELD_NAME_MAX
import com.serebit.strife.entities.Embed.Companion.FIELD_VAL_MAX
import com.serebit.strife.entities.Embed.Companion.FOOTER_MAX
import com.serebit.strife.entities.Embed.Companion.TITLE_MAX
import com.serebit.strife.entities.Embed.Field
import com.serebit.strife.entities.embed
import com.serebit.strife.internal.times
import kotlin.test.Test
import kotlin.test.assertFailsWith


class EmbedTest {

    private fun generateFields(length: Int = Embed.FIELD_MAX) = MutableList(length) { Field("$it", "$it", false) }

    @Test
    fun `invalid title`() {
        assertFailsWith<IllegalArgumentException> { embed { title = "" } }
        assertFailsWith<IllegalArgumentException> { embed { title = "X" * (TITLE_MAX + 1) } }
    }

    @Test
    fun `invalid description`() {
        assertFailsWith<IllegalArgumentException> { embed { title = "X"; description = "" } }
        assertFailsWith<IllegalArgumentException> { embed { title = "X"; description = "X" * (DESCRIPTION_MAX + 1) } }
    }

    @Test
    fun `too many fields`() {
        assertFailsWith<IllegalArgumentException> {
            embed {
                title = "X"
                description = "X"
                fields.addAll(generateFields(Embed.FIELD_MAX + 1))
            }
        }
    }

    @Test
    fun `invalid field name`() {
        assertFailsWith<IllegalArgumentException> { Field("", "X") }
        assertFailsWith<IllegalArgumentException> { Field("X" * (FIELD_NAME_MAX + 1), "X") }
    }

    @Test
    fun `invalid field content`() {
        assertFailsWith<IllegalArgumentException> { Field("X", "") }
        assertFailsWith<IllegalArgumentException> { Field("X", "X" * (FIELD_VAL_MAX + 1)) }
    }

    @Test
    fun `invalid footer`() {
        assertFailsWith<IllegalArgumentException> { embed { footer { text = "" } } }
        assertFailsWith<IllegalArgumentException> { embed { footer { text = "X" * (FOOTER_MAX + 1) } } }
    }

}
