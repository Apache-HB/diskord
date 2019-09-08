package com.serebit.strife

/** Returns the [text] and [url] formatted into a discord markdown hyperlink. */
fun link(text: String, url: String) = "[$text]($url)"

/** Returns the [text] formatted into markdown _italics_. */
fun italic(text: String?) = text?.let { "*$text*" }

/** Returns the [text] formatted into markdown *bold*. */
fun bold(text: String?) = text?.let { "**$text**" }

/** Returns the [text] formatted into markdown *_bold italics_*. */
fun boldItalic(text: String?) = italic(bold(text))

/** Returns the [text] formatted into markdown underline. */
fun underline(text: String?) = text?.let { "__${text}__" }

/** Returns the [text] formatted into markdown ~~strikethrough~~. */
fun crossout(text: String?) = text?.let { "~~$text~~" }

/** Returns the [text] formatted into markdown inline ``code``. */
fun code(text: String?) = text?.let { "``$text``" }

/** Returns the [text] formatted into markdown ``code block``. */
fun codeBlock(text: String?, language: String = "") = text?.let { "```$language\n$text```" }

/** Dummy Thicc easter egg */
fun thicc(text: String?) = underline(boldItalic(text))
