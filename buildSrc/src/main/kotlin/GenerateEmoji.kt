package com.serebit.strife.buildsrc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.io.File

private fun readResourceText(path: String) = EmojiEntry::class.java.classLoader
    .getResourceAsStream(path)!!
    .readBytes()
    .toString(Charsets.UTF_8)

private val nameExceptionMap = mapOf(
    "8ball" to "EightBall",
    "1234" to "OneTwoThreeFour",
    "100" to "OneHundred"
)

fun generateUnicodeEmoji(resultDirPath: String) {
    val emojiStub = readResourceText("Emoji.ktstub")
    val emojiMapStub = readResourceText("EmojiMap.ktstub")

    // sourced from Discord JS files. This is the JSON they use to tell the client what emojis are supported,
    // so it's the perfect source for this codegen
    val text = readResourceText("discord-emoji.json")
    val entries = Json(JsonConfiguration.Stable)
        .parse((String.serializer() to EmojiEntry.serializer().list).map, text)
        .values
        .flatten()

    File(resultDirPath).resolve("Emoji.kt").writeText(buildString {
        appendln("// AUTOGENERATED BY GRADLE, VIEW buildSrc/src/main/GenerateEmoji.kt")
        append(emojiStub.substringBefore("// REPLACEMENT_MARKER"))
        entries.forEach { appendln(it.commentLine); appendln(it.objLine) }
        append(emojiStub.substringAfter("// REPLACEMENT_MARKER"))
    })

    File(resultDirPath).resolve("EmojiMap.kt").writeText(buildString {
        appendln("// AUTOGENERATED BY GRADLE, VIEW buildSrc/src/main/GenerateEmoji.kt")
        append(emojiMapStub.substringBefore("// REPLACEMENT_MARKER"))
        appendln(entries.joinToString(",\n") { it.mapLine })
        append(emojiMapStub.substringAfter("// REPLACEMENT_MARKER"))
    })
}

private fun String.correctCase(): String = when {
    startsWith("Flag") -> dropLast(1) + last().toUpperCase()
    length == 2 -> toUpperCase()
    else -> this
}

private fun String.applyExceptions(): String = if (this in nameExceptionMap) nameExceptionMap.getValue(this) else this

@Serializable
private data class EmojiEntry(
    val names: List<String>,
    val surrogates: String,
    val unicodeVersion: Double,
    val hasDiversity: Boolean = false,
    val hasMultiDiversity: Boolean = false,
    val diversityChildren: List<EmojiEntry> = emptyList(),
    val diversity: List<String> = emptyList(),
    val hasDiversityParent: Boolean = false,
    val hasMultiDiversityParent: Boolean = false
) {
    private val nameToUse =
        if (names.any { !it.matches("[\\d\\w]+".toRegex()) }) names.maxBy { it.length }!!
        else names.minBy { it.length }!!

    private val className = nameToUse.split("_")
        .joinToString("") { it.capitalize() }
        .correctCase()
        .applyExceptions()
    private val unicodeLiteral = surrogates
        .map { it.toInt().toString(16).padStart(4, '0') }
        .joinToString("") { "\\u$it" }
    private val shortcodes = names.sortedBy { it.length }.map { "`:$it:`" }
    val commentLine = buildString {
        append("/** Unicode emoji with ")
        when (names.size) {
            1 -> append("a Discord shortcode of ${shortcodes.single()}")
            2 -> append("Discord shortcodes of ${shortcodes.first()} and ${shortcodes.last()}")
            else -> append("Discord shortcodes of ${shortcodes.dropLast(1).joinToString()}, and ${shortcodes.last()}")
        }
        append(if (hasDiversity) ", and the given skin [tone]. " else ". ")
        append("Represented in Unicode as $surrogates. */")
    }
    val objLine = buildString {
        append(if (hasDiversity) "class " else "object ")
        append(className)
        if (hasDiversity) append("(tone: SkinTone? = null)")
        append(" : UnicodeEmoji(\"${unicodeLiteral}\"")
        if (hasDiversity) append(", tone")
        append(")")
    }
    val mapLine = buildString {
        append("    \"${unicodeLiteral}\" to ")
        if (hasDiversity) append("WithSkinTone { skinTone -> UnicodeEmoji.${className}(skinTone) }")
        else append("Normal(UnicodeEmoji.$className)")
    }
}
