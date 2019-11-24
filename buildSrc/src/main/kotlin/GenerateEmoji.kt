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
    val entries = Json(JsonConfiguration.Stable).parse((String.serializer() to EmojiEntry.serializer().list).map, text)
        .entries
        .map { EmojiCategory(it.key, it.value) }
        .map { it.entries }
        .flatten()

    File(resultDirPath).resolve("Emoji.kt").also { file: File ->
        file.createNewFile()
        val newText = emojiStub.replace("// REPLACEMENT_MARKER", buildString {
            entries.forEach {
                append("${it.comment}\n")
                append("${it.obj}\n\n")
            }
        })
        file.writeText(newText)
    }

    File(resultDirPath).resolve("EmojiMap.kt").also { file: File ->
        file.createNewFile()
        val newText = emojiMapStub.replace("// REPLACEMENT_MARKER", buildString {
            entries.dropLast(1).forEach {
                append("${it.mapLine},\n")
            }
            entries.last().also {
                append("${it.mapLine}\n")
            }
        })
        file.writeText(newText)
    }
}

@Serializable
data class EmojiEntry(
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
        if (names.any { !it.matches("[\\d\\w]+".toRegex()) })
            names.maxBy { it.length }!!
        else
            names.minBy { it.length }!!

    private val className = nameToUse.split("_")
        .joinToString("") { it.capitalize() }
        .let {
            when {
                it.startsWith("Flag") -> it.dropLast(1) + it.last().toUpperCase()
                it.length == 2 -> it.toUpperCase()
                else -> it
            }
        }.let {
            if (it in nameExceptionMap.keys) nameExceptionMap[it] else it
        }
    private val unicodeLiteral = surrogates.map { it.toInt() }
        .joinToString("") {
            "\\u${it.toString(16).padStart(4, '0')}"
        }
    private val shortcodes = names.sortedBy { it.length }.map { "`:$it:`" }
    val comment = buildString {
        append("/** Unicode emoji with ")
        when (names.size) {
            1 -> append("a Discord shortcode of ${shortcodes.single()}")
            2 -> append("Discord shortcodes of ${shortcodes.first()} and ${shortcodes.last()}")
            else -> append("Discord shortcodes of ${shortcodes.dropLast(1).joinToString()}, and ${shortcodes.last()}")
        }
        append(if (hasDiversity) ", and the given skin [tone]. " else ". ")
        append("Represented in Unicode as $surrogates. */")
    }
    val obj = buildString {
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

class EmojiCategory(val name: String, val entries: List<EmojiEntry>)
