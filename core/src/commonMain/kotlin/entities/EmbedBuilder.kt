package com.serebit.strife.entities

import com.serebit.strife.data.Color
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.EmbedPacket
import com.serebit.strife.internal.packets.OutgoingEmbedPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime

/** Marks a function which implements DSL-style code related to an [EmbedBuilder]. */
@DslMarker
annotation class EmbedDsl

/**
 * An [EmbedBuilder] is used to create a usable embed. An embed is a card-like content
 * display sent by Webhooks and Bots. [Here](https://imgur.com/a/yOb5n) you can see each part of the embed explained
 * and shown.
 *
 * You can use an embed preview took [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 */
@EmbedDsl
class EmbedBuilder(init: EmbedBuilder.() -> Unit = {}) {
    /** The [title] of the embed appears atop the [description] and right below the [author]. */
    var title: TitleBuilder? = null
        set(value) {
            require(value?.title == null || value.title?.length in 1..TITLE_MAX) {
                "Title must be within ${1..TITLE_MAX} char. (was ${value?.title?.length})"
            }
            field = value
        }
    /**
     * The description of the embed appears after the [title] and before any [FieldBuilder]. The [description] supports
     * standard Discord markdown as well as [markdown\](links).
     */
    var description: String? = null
        set(value) {
            require(value == null || value.length in 1..DESCRIPTION_MAX) {
                "Description must be within ${1..DESCRIPTION_MAX} char. (was ${description?.length})"
            }
            field = value
        }
    /**
     * The author whose name and image will appear at the very top of the embed.
     */
    var author: AuthorBuilder? = null
        set(value) {
            require(value == null || value.name?.length in 1..AUTHOR_NAME_MAX) {
                "Name must be within ${1..AUTHOR_NAME_MAX} char. (was ${value?.name?.length})"
            }
            field = value
        }
    /** The color of the embed's left border. Leaving this `null` will result in the default greyish color. */
    var color: Color? = null
    /** A list of all fields in the embed in order of appearance (top -> bottom, left -> right). */
    var fields: MutableList<FieldBuilder> = mutableListOf()
    /** The image which is shown at the bottom of the embed. */
    var image: GraphicBuilder? = null
    /**
     * The thumbnail appears in the upper-right-hand corner of the embed as a smaller image.
     * Set this to `null` for no thumbnail.
     */
    var thumbnail: GraphicBuilder? = null
    var video: GraphicBuilder? = null
    /** The footer of the embed shown at the very bottom. */
    var footer: FooterBuilder? = null
        set(value) {
            require(value == null || value.text?.length in 1..FOOTER_MAX) {
                "Name must be within ${1..FOOTER_MAX} char. (was ${value?.text?.length})"
            }
            field = value
        }
    /**
     * The timestamp is shown to the right of the [footer] and is usually used to mark when the embed was sent, but
     * can be set to any date and time.
     */
    var timestamp: DateTime? = null

    init {
        this.init()
    }

    /**
     * @property title The title's text.
     * @property url The url which will be opened when the title is clicked. Set this to `null` for no link.
     */
    class TitleBuilder(var title: String? = null, var url: String? = null)

    /**
     * @property url The hyperlink embedded in the [name].
     * @property imgUrl An image that will be shown to the left of the [name].
     */
    class AuthorBuilder(
        var name: String? = null,
        var url: String? = null,
        var imgUrl: String? = null,
        var proxyImgUrl: String? = null
    ) {
        internal fun build() = OutgoingEmbedPacket.Author(name, url, imgUrl, proxyImgUrl)
    }

    /**
     * Builder for a field in an [EmbedBuilder].
     *
     * @property name The name (title) of the field.
     * @property inline Whether the field should be displayed inline (i.e., next to other inline fields where possible).
     * @property content The text displayed in the body of the field.
     */
    class FieldBuilder(var inline: Boolean = false) {
        var name: String? = null
            set(value) {
                require(value?.length in 1..FIELD_NAME_MAX) {
                    "Name must be within ${1..FIELD_NAME_MAX} char. (was ${value?.length})"
                }
                field = value
            }
        var content: String? = null
            set(value) {
                require(value != null && value.length in 1..FIELD_VAL_MAX) {
                    "Content must be within ${1..FIELD_VAL_MAX} char. (was ${value?.length})"
                }
                field = value
            }

        internal fun build() = OutgoingEmbedPacket.Field(name!!, content!!, inline)
    }

    /**
     * An image or video within the embed.
     *
     * @property url The URL of the image.
     */
    class GraphicBuilder(
        var url: String? = null,
        var proxyImgUrl: String? = null
    ) {
        internal fun build() = OutgoingEmbedPacket.EmbedGraphic(url, proxyImgUrl)
    }

    /**
     * @property text The text to be shown at the bottom of the embed.
     * @property imgUrl The URL for the image to the left of the text.
     */
    class FooterBuilder(
        var text: String? = null,
        var imgUrl: String? = null,
        var proxyImgUrl: String? = null
    ) {
        internal fun build() = OutgoingEmbedPacket.Footer(text, imgUrl, proxyImgUrl)
    }

    /** Build the [EmbedBuilder] into a usable [OutgoingEmbedPacket]. */
    internal fun build() = OutgoingEmbedPacket(
        title = title?.title,
        titleUrl = title?.url,
        description = description,
        time_stamp = timestamp?.format(DateFormat.ISO_WITH_MS),
        color_int = color?.rgb,
        footer = footer?.build(),
        image = image?.build(),
        thumbnail = thumbnail?.build(),
        video = video?.build(),
        author = author?.build(),
        fields = fields.map { it.build() }
    )

    companion object {
        const val TITLE_MAX = 256
        const val DESCRIPTION_MAX = 2048
        /** The maximum number of [fields][FieldBuilder] an [EmbedBuilder] can have. */
        const val FIELD_MAX = 25
        const val FIELD_NAME_MAX = 256
        const val FIELD_VAL_MAX = 1024
        const val FOOTER_MAX = 2048
        const val AUTHOR_NAME_MAX = 256
    }
}

/**
 * Use this function to add a [Field][EmbedBuilder.FieldBuilder].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     field(name = String, inline = Boolean) {
 *          "content"
 *     }
 * ```
 */
@EmbedDsl
inline fun EmbedBuilder.field(name: String, inline: Boolean = false, content: () -> String) = field(inline) {
    this.name = name
    this.content = content()
}

/**
 * Use this function to add an inline [Field][EmbedBuilder.FieldBuilder].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     inlineField(name = "NAME") {
 *          "content"
 *     }
 * ```
 */
@EmbedDsl
inline fun EmbedBuilder.inlineField(name: String, content: () -> String) = field(true) {
    this.name = name
    this.content = content()
}

/**
 * Use this function to set the [author].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     author {
 *          name = "NAME"
 *          url = "URL"
 *          imgUrl = "IMG_URL"
 *     }
 * ```
 */
@EmbedDsl
fun EmbedBuilder.author(builder: EmbedBuilder.AuthorBuilder.() -> Unit) {
    author = EmbedBuilder.AuthorBuilder().also(builder)
}

/**
 * Use this function to add a [inline][FieldBuilder.inline] [Field][Field].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     inlineField {
 *          name = "NAME"
 *          content = "CONTENT"
 *     }
 * ```
 */
@EmbedDsl
inline fun EmbedBuilder.inlineField(builder: EmbedBuilder.FieldBuilder.() -> Unit) = field(true, builder)

/**
 * Use this function to add a [Field][Field].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     field(inline = true) {
 *          name = "NAME"
 *          content = "CONTENT"
 *          inline = true/false
 *     }
 * ```
 */
@EmbedDsl
inline fun EmbedBuilder.field(inline: Boolean = false, init: EmbedBuilder.FieldBuilder.() -> Unit) =
    fields.add(EmbedBuilder.FieldBuilder(inline = inline).apply(init))

/** Set the thumbnail image. */
@EmbedDsl
inline fun EmbedBuilder.thumbnail(url: String? = null, init: EmbedBuilder.GraphicBuilder.() -> Unit = {}) {
    thumbnail = EmbedBuilder.GraphicBuilder().apply { this.url = url }.apply(init)
}

/**
 * Use this function to set the [Footer].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     footer {
 *          text = "NAME"
 *          imgUrl = "IMG_URL"
 *     }
 * ```
 */
@EmbedDsl
fun EmbedBuilder.footer(builder: EmbedBuilder.FooterBuilder.() -> Unit) {
    footer = EmbedBuilder.FooterBuilder().apply(builder)
}

/** Set the [video][video]. */
@EmbedDsl
inline fun EmbedBuilder.video(url: String? = null, builder: EmbedBuilder.GraphicBuilder.() -> Unit = {}) {
    video = EmbedBuilder.GraphicBuilder().apply { this.url = url }.apply(builder)
}

/** Set the [image][image]. */
@EmbedDsl
inline fun EmbedBuilder.image(url: String? = null, builder: EmbedBuilder.GraphicBuilder.() -> Unit = {}) {
    image = EmbedBuilder.GraphicBuilder().apply { this.url = url }.apply(builder)
}

/**
 * Use this function to set the [title].
 *
 * This function can be used in DSL or normal form.
 * ```kotlin
 *     title("TITLE", "URL")
 *     or
 *     title {
 *         title = "TITLE"
 *         url = ""URL
 *     }
 * ```
 */
@EmbedDsl
inline fun EmbedBuilder.title(
    text: String? = null,
    url: String? = null,
    init: EmbedBuilder.TitleBuilder.() -> Unit = {}
) {
    this.title = EmbedBuilder.TitleBuilder(text, url).apply(init)
}

/**
 * Create an [EmbedBuilder] in a type-safe DSL function.
 *```
 *     Property    |   Set With
 * ----------------|---------------
 *     title       |   title(), =
 *     title Url   |   title(), =
 *    description  |       =
 *      author     |   author()
 *      color      |       =
 *      fields     | field(), inlineField()
 *      image      |    image()
 *      video      |    video()
 *    thumbnail    |    thumbnail()
 *     footer      |    footer()
 *    timestamp    |       =
 * ```
 */
@EmbedDsl
fun embed(builder: EmbedBuilder.() -> Unit) = EmbedBuilder().apply(builder)

/** Convert the [EmbedPacket] to an [EmbedBuilder]. */
internal fun EmbedPacket.toEmbed() = embed {

}
