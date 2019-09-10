package com.serebit.strife.text

/** Returns this String formatted as a discord inline hyperlink, with the given [url]. */
fun String.inlineLink(url: String) = "[$this]($url)"

/** Returns this String formatted into markdown *italics*. */
val String.italic get() = "*${this}*"

/** Returns this String formatted into markdown **bold**. */
val String.bold get() = "**${this}**"

/** Returns this String formatted into markdown ***bold italics***. */
val String.boldItalic get() = bold.italic

/** Returns this String formatted into markdown underline. */
val String.underline get() = "__${this}__"

/** Returns this String formatted into markdown ~~strikethrough~~. */
val String.strikethrough get() = "~~${this}~~"

/** Returns this String formatted into markdown inline `code`. */
val String.inlineCode get() = "`${this}`"

/** Returns this String formatted into a markdown ```code block```. */
fun String.codeBlock(language: String = "") ="```$language\n${this}\n```"
