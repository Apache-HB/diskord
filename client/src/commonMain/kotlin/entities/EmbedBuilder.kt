package com.serebit.strife.entities

import com.serebit.strife.data.BoundedList
import com.serebit.strife.data.Color
import com.serebit.strife.data.boundedListOf
import com.serebit.strife.internal.ISO_WITH_MS
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
class EmbedBuilder {
    /** The title of the embed appears atop the [description] and right below the [author]. */
    var titleText: String? = EmbedDefaults.titleText
        set(value) {
            require(value == null || value.length in 1..TITLE_MAX) {
                "Title text must be within ${1..TITLE_MAX} char. (was ${value?.length})"
            }
            field = value
        }
    /** The (optional) URL hyperlink of the [titleText] */
    var titleUrl: String? = EmbedDefaults.titleUrl
        set(value) {
            require(value == null || titleText != null) {
                "The title URL cannot be given a not-null value if the title text is null."
            }
            field = value
        }
    /**
     * The description of the embed appears after the [title] and before any field. It supports standard Discord
     * markdown as well as [inline\](links).
     */
    var description: String? = EmbedDefaults.description
        set(value) {
            require(value == null || value.length in 1..DESCRIPTION_MAX) {
                "Description must be within ${1..DESCRIPTION_MAX} char. (was ${description?.length})"
            }
            field = value
        }
    /**
     * The author whose name and image will appear at the very top of the embed.
     */
    var author: AuthorBuilder? = EmbedDefaults.author
        set(value) {
            require(value == null || value.name?.length in 1..AUTHOR_NAME_MAX) {
                "Name must be within ${1..AUTHOR_NAME_MAX} char. (was ${value?.name?.length})"
            }
            field = value
        }
    /** The color of the embed's left border. Leaving this `null` will result in the default greyish color. */
    var color: Color? = EmbedDefaults.color
    /** A list of all fields in the embed in order of appearance (top -> bottom, left -> right). */
    var fields: BoundedList<FieldBuilder> = EmbedDefaults.fields
    /** The image which is shown at the bottom of the embed. */
    var image: String? = EmbedDefaults.image
    /**
     * The thumbnail appears in the upper-right-hand corner of the embed as a smaller image.
     * Set this to `null` for no thumbnail.
     */
    var thumbnail: String? = EmbedDefaults.thumbnail
    /** The footer of the embed shown at the very bottom. */
    var footer: FooterBuilder? = EmbedDefaults.footer
        set(value) {
            require(value == null || value.text?.length in 1..FOOTER_MAX) {
                "Footer text must be within ${1..FOOTER_MAX} char. (was ${value?.text?.length})"
            }
            field = value
        }
    /**
     * The timestamp is shown to the right of the [footer] and is usually used to mark when the embed was sent, but
     * can be set to any date and time.
     */
    var timestamp: DateTime? = EmbedDefaults.timestamp

    /**
     * @property name The Author's name.
     * @property url The hyperlink embedded in the [name].
     * @property imgUrl An image that will be shown to the left of the [name].
     */
    class AuthorBuilder(
        var name: String? = null,
        var url: String? = null,
        var imgUrl: String? = null
    ) {
        internal fun build() = OutgoingEmbedPacket.Author(name, url, imgUrl)
    }

    /**
     * Builder for a field in an [EmbedBuilder].
     *
     * @property name The name (title) of the field.
     * @property inline Whether the field should be displayed inline (i.e., next to other inline fields where possible).
     * @property content The text displayed in the body of the field.
     */
    class FieldBuilder(name: String, content: String, var inline: Boolean = false) {
        var name: String = name
            set(value) {
                require(value.length in 1..FIELD_NAME_MAX) {
                    "Name must be within ${1..FIELD_NAME_MAX} char. (was ${value.length})"
                }
                field = value
            }
        var content: String = content
            set(value) {
                require(value.length in 1..FIELD_VAL_MAX) {
                    "Content must be within ${1..FIELD_VAL_MAX} char. (was ${value.length})"
                }
                field = value
            }

        init {
            require(name.length in 1..FIELD_NAME_MAX) {
                "Name must be within ${1..FIELD_NAME_MAX} char. (was ${name.length})"
            }
            require(content.length in 1..FIELD_VAL_MAX) {
                "Content must be within ${1..FIELD_VAL_MAX} char. (was ${content.length})"
            }
        }

        internal fun build() = OutgoingEmbedPacket.Field(name, content, inline)
    }

    /**
     * @property text The text to be shown at the bottom of the embed.
     * @property imgUrl The URL for the image to the left of the text.
     */
    class FooterBuilder(var text: String? = null, var imgUrl: String? = null) {
        internal fun build() = OutgoingEmbedPacket.Footer(text, imgUrl)
    }

    /** Build the [EmbedBuilder] into a usable [OutgoingEmbedPacket]. */
    internal fun build() = OutgoingEmbedPacket(
        title = titleText,
        titleUrl = titleUrl,
        description = description,
        time_stamp = timestamp?.format(DateFormat.ISO_WITH_MS),
        color_int = color?.rgb,
        footer = footer?.build(),
        image = OutgoingEmbedPacket.EmbedGraphic(image),
        thumbnail = OutgoingEmbedPacket.EmbedGraphic(thumbnail),
        author = author?.build(),
        fields = fields.map { it.build() }
    )

    companion object {

        /** The default values of all [EmbedBuilder] settings. */
        @EmbedDsl object EmbedDefaults {
            /** The title of the embed appears atop the [description] and right below the [author]. */
            var titleText: String? = null
            /** The (optional) URL hyperlink of the [titleText] */
            var titleUrl: String? = null
            /**
             * The description of the embed appears after the [title] and before any field. It supports standard Discord
             * markdown as well as [inline\](links).
             */
            var description: String? = null
            /** * The author whose name and image will appear at the very top of the embed. */
            var author: AuthorBuilder? = null
            /** The color of the embed's left border. Leaving this `null` will result in the default greyish color. */
            var color: Color? = null
            /** A list of all fields in the embed in order of appearance (top -> bottom, left -> right). */
            var fields: BoundedList<FieldBuilder> = boundedListOf(FIELD_MAX)
            /** The image which is shown at the bottom of the embed. */
            var image: String? = null
            /**
             * The thumbnail appears in the upper-right-hand corner of the embed as a smaller image.
             * Set this to `null` for no thumbnail.
             */
            var thumbnail: String? = null
            /** The footer of the embed shown at the very bottom. */
            var footer: FooterBuilder? = null
            /**
             * The timestamp is shown to the right of the [footer] and is usually used to mark when the embed was sent, but
             * can be set to any date and time.
             */
            var timestamp: DateTime? = null

            /** Use this to make setting variables easy with DSL. */
            @EmbedDsl
            operator fun invoke(block: EmbedBuilder.(EmbedDefaults) -> Unit) {
                EmbedBuilder().also { block(it, this) }.also {
                    titleText = it.titleText
                    titleUrl = it.titleUrl
                    description = it.description
                    author = it.author
                    color = it.color
                    fields = it.fields
                    image = it.image
                    thumbnail = it.thumbnail
                    footer = it.footer
                    timestamp = it.timestamp
                }
            }
        }

        /** The maximum of characters for the [EmbedBuilder.titleText]. */
        const val TITLE_MAX: Int = 256
        /** The maximum of characters for the [EmbedBuilder.description]. */
        const val DESCRIPTION_MAX: Int = 2048
        /** The maximum number of [fields] an [EmbedBuilder] can have. */
        const val FIELD_MAX: Int = 25
        /** The maximum of characters for the [FieldBuilder.name]. */
        const val FIELD_NAME_MAX: Int = 256
        /** The maximum of characters for the [FieldBuilder.content]. */
        const val FIELD_VAL_MAX: Int = 1024
        /** The maximum of characters for the [EmbedBuilder.FooterBuilder]. */
        const val FOOTER_MAX: Int = 2048
        /** The maximum of characters for the [EmbedBuilder.AuthorBuilder.name]. */
        const val AUTHOR_NAME_MAX: Int = 256
    }
}

/**
 * Use this function to set the title's text and URL. The title URL must not be null if the title text is null.
 */
@EmbedDsl
fun EmbedBuilder.title(text: String? = null, url: String? = null) {
    titleText = text
    titleUrl = url
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
inline fun EmbedBuilder.field(name: String, inline: Boolean, content: () -> Any) {
    fields.add(EmbedBuilder.FieldBuilder(name, content().toString(), inline))
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
inline fun EmbedBuilder.inlineField(name: String, content: () -> Any): Unit = field(name, true, content)

/**
 * Use this function to add a non-inline [Field][EmbedBuilder.FieldBuilder].
 *
 * This function is used in DSL form.
 * ```kotlin
 *     field(name = "NAME") {
 *          "content"
 *     }
 * ```
 */
@EmbedDsl
inline fun EmbedBuilder.field(name: String, content: () -> Any) {
    fields.add(EmbedBuilder.FieldBuilder(name, content().toString(), false))
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

/** Set the thumbnail image. */
@EmbedDsl
fun EmbedBuilder.thumbnail(url: String? = null) {
    thumbnail = url
}

/**
 * Use this function to set the footer.
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

/** Set the [image][image]. */
@EmbedDsl
fun EmbedBuilder.image(url: String? = null) {
    image = url
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
 *    thumbnail    |    thumbnail()
 *     footer      |    footer()
 *    timestamp    |       =
 * ```
 */
@EmbedDsl
fun embed(builder: EmbedBuilder.() -> Unit): EmbedBuilder = EmbedBuilder().apply(builder)

/** Convert the [Embed] to an [EmbedBuilder]. */
fun Embed.toEmbedBuilder(): EmbedBuilder = EmbedBuilder().apply {
    author {
        name = this@toEmbedBuilder.author?.name
        url = this@toEmbedBuilder.author?.url
        imgUrl = this@toEmbedBuilder.author?.imgUrl
    }
    title(this@toEmbedBuilder.title?.text, this@toEmbedBuilder.title?.url)
    description = this@toEmbedBuilder.description
    this@toEmbedBuilder.fields.forEach { field(it.name, it.inline) { it.value } }
    color = this@toEmbedBuilder.color
    image(this@toEmbedBuilder.image?.url)
    thumbnail(this@toEmbedBuilder.thumbnail?.url)
    footer {
        text = this@toEmbedBuilder.footer?.text
        imgUrl = this@toEmbedBuilder.footer?.iconUrl
    }
    this.timestamp = this@toEmbedBuilder.timestamp
}

