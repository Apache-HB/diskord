import com.serebit.strife.commands.ParamType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParamTypeTests {
    private val validIntegerStrings = listOf(
        "1",
        "-1",
        "+1"
    )
    private val invalidIntegerStrings = listOf(
        "1.0",
        "-1.0",
        "+1.0",
        "-.1",
        "1e2",
        "-1e2",
        "+1e2",
        "bees",
        "8ball"
    )
    private val validDecimalStrings = listOf(
        "1",
        ".1",
        "1.",
        "1.0",
        "+1",
        "+.1",
        "+1.0",
        "-1",
        "-.1",
        "-1.0",
        ".1e2",
        ".0e2",
        "+1.",
        "-1."
    )
    private val invalidDecimalStrings = listOf(
        ".0e2.3",
        ".e2",
        "e2",
        ".",
        ") ",
        "+",
        "-",
        "a"
    )

    @Test
    fun `ensure signature accuracy of ByteParam`() {
        val signature = ParamType.NumberType.ByteParam.signature

        val validStrings = validIntegerStrings + listOf(Byte.MAX_VALUE.toString(), Byte.MIN_VALUE.toString())
        val invalidStrings = invalidIntegerStrings + listOf("${Byte.MAX_VALUE}0", "${Byte.MIN_VALUE}0")

        validStrings.forEach { assertTrue(signature.matches(it), "Signature should match value $it") }
        invalidStrings.forEach { assertFalse(signature.matches(it), "Signature should not match value $it") }
    }

    @Test
    fun `ensure signature accuracy of ShortParam`() {
        val signature = ParamType.NumberType.ShortParam.signature

        val validStrings = validIntegerStrings + listOf(Short.MAX_VALUE.toString(), Short.MIN_VALUE.toString())
        val invalidStrings = invalidIntegerStrings + listOf("${Short.MAX_VALUE}0", "${Short.MIN_VALUE}0")

        validStrings.forEach { assertTrue(signature.matches(it), "Signature should match value $it") }
        invalidStrings.forEach { assertFalse(signature.matches(it), "Signature should not match value $it") }
    }

    @Test
    fun `ensure signature accuracy of IntParam`() {
        val signature = ParamType.NumberType.IntParam.signature

        val validStrings = validIntegerStrings + listOf(Int.MAX_VALUE.toString(), Int.MIN_VALUE.toString())
        val invalidStrings = invalidIntegerStrings + listOf("${Int.MAX_VALUE}0", "${Int.MIN_VALUE}0")

        validStrings.forEach { assertTrue(signature.matches(it), "Signature should match value $it") }
        invalidStrings.forEach { assertFalse(signature.matches(it), "Signature should not match value $it") }
    }

    @Test
    fun `ensure signature accuracy of LongParam`() {
        val signature = ParamType.NumberType.LongParam.signature

        val validStrings = validIntegerStrings + listOf(Long.MAX_VALUE.toString(), Long.MIN_VALUE.toString())
        val invalidStrings = invalidIntegerStrings + listOf("${Long.MAX_VALUE}0", "${Long.MIN_VALUE}0")

        validStrings.forEach { assertTrue(signature.matches(it), "Signature should match value $it") }
        invalidStrings.forEach { assertFalse(signature.matches(it), "Signature should not match value $it") }
    }

    @Test
    fun `ensure signature accuracy of FloatParam`() {
        val signature = ParamType.NumberType.FloatParam.signature

        validDecimalStrings.forEach { assertTrue(signature.matches(it), "Signature should match value $it") }
        invalidDecimalStrings.forEach { assertFalse(signature.matches(it), "Signature should not match value $it") }
    }

    @Test
    fun `ensure signature accuracy of DoubleParam`() {
        val signature = ParamType.NumberType.DoubleParam.signature

        validDecimalStrings.forEach { assertTrue(signature.matches(it), "Signature should match value $it") }
        invalidDecimalStrings.forEach { assertFalse(signature.matches(it), "Signature should not match value $it") }
    }
}
