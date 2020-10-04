package com.serebit.strife.buildsrc

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File

private fun readResourceText(path: String) = EmojiEntry::class.java.classLoader
    .getResourceAsStream(path)
    ?.readBytes()
    ?.toString(Charsets.UTF_8)

private val nameExceptionMap = mapOf(
    "8ball" to "EightBall",
    "1234" to "OneTwoThreeFour",
    "100" to "OneHundred"
)

fun generateUnicodeEmoji(resultDirPath: String) {
    val resultDir = File(resultDirPath)
    val emojiStub = readResourceText("Emoji.ktstub") ?: error("Failed to resolve Emoji.kt stub file.")
    val emojiMapStub = readResourceText("EmojiMap.ktstub") ?: error("Failed to resolve EmojiMap.kt stub file.")

    // sourced from Discord JS files. This is the JSON they use to tell the client what emojis are supported,
    // so it's the perfect source for this codegen
    val text = readResourceText("discord-emoji.json") ?: error("Failed to resolve discord-emoji.json file.")
    val entries = Json
        .decodeFromString(MapSerializer(String.serializer(), ListSerializer(EmojiEntry.serializer())), text)
        .values
        .flatten()

    val generatedProperties = entries.joinToString("\n") { "${it.commentLine}\n${it.objLine}" }
    val generatedMapEntries = entries.joinToString(",\n") { it.mapLine }

    resultDir.resolve("Emoji.kt").writeText(buildString {
        appendln("// AUTOGENERATED BY GRADLE, VIEW buildSrc/src/main/kotlin/GenerateEmoji.kt")
        append(emojiStub.replace("// REPLACEMENT_MARKER", generatedProperties))
    })

    resultDir.resolve("EmojiMap.kt").writeText(buildString {
        appendln("// AUTOGENERATED BY GRADLE, VIEW buildSrc/src/main/kotlin/GenerateEmoji.kt")
        append(emojiMapStub.replace("// REPLACEMENT_MARKER", generatedMapEntries))
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

    private val propertyName = nameToUse.split("_")
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
        append("Represented in Unicode as $surrogates. */")
    }
    val objLine = buildString {
        append("val $propertyName")
        if (hasDiversity) append(": VariantSkinTone = VariantSkinTone(\"${unicodeLiteral}\", null)")
        else append(": Invariant = Invariant(\"$unicodeLiteral\")")
    }
    val mapLine = buildString {
        append("    \"${unicodeLiteral}\" to ")
        if (hasDiversity) append("WithSkinTone { skinTone -> UnicodeEmoji.${propertyName}.withTone(skinTone) }")
        else append("Normal(UnicodeEmoji.$propertyName)")
    }
}