package com.serebit.strife.text

import com.serebit.strife.entities.UnicodeEmoji
import com.serebit.strife.entities.asString

/** Returns this String formatted as a discord inline hyperlink, with the given [url]. */
fun String.inlineLink(url: String): String = "[$this]($url)"

/** Returns this String formatted into markdown *italics*. */
val String.italic: String get() = "*${this}*"

/** Returns this String formatted into markdown **bold**. */
val String.bold: String get() = "**${this}**"

/** Returns this String formatted into markdown ***bold italics***. */
val String.boldItalic: String get() = bold.italic

/** Returns this String formatted into markdown underline. */
val String.underline: String get() = "__${this}__"

/** Returns this String formatted into markdown ~~strikethrough~~. */
val String.strikethrough: String get() = "~~${this}~~"

/** Returns this String formatted into markdown inline `code`. */
val String.inlineCode: String get() = "`${this}`"

/** Returns this String formatted into a markdown ```code block```. */
fun String.codeBlock(language: String = ""): String = "```$language\n${this}\n```"

/** Returns this [Int] as an emoji String */
val Int.asEmojiString: String get() = UnicodeEmoji.fromInt(this).asString
