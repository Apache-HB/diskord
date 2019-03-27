package com.serebit.strife.entities

import com.serebit.strife.StrifeDsl
import com.serebit.strife.data.Color
import com.serebit.strife.entities.Embed.Author
import com.serebit.strife.entities.Embed.EmbedGraphic
import com.serebit.strife.entities.Embed.Field
import com.serebit.strife.entities.Embed.Footer
import com.serebit.strife.entities.Embed.Provider
import com.serebit.strife.internal.ISO_WITHOUT_MS
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.EmbedPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * An [Embed] is a card-like content display sent by Webhooks and Bots. [Here](https://imgur.com/a/yOb5n) you can see
 * each part of the embed explained and shown.
 *
 * You can use an embed preview tool [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 *
 * @property title The title of the embed appears atop the [description] and right below the [author].
 * @property titleUrl The url which when the [title] is clicked will be opened. Set this to `null` for no link.
 * @property description The description of the embed appears after the [title] and before any [Field]. The
 * [description] supports standard Discord markdown as well as [markdown\](links).
 * @property thumbnail The thumbnail appears in the upper-right-hand corner of the embed as a smaller image. Set this
 * to `null` for no thumbnail.
 * @property author The author who's name will appear at the very top of the [Embed]. The [Author.imgUrl] will be
 * shown to the left of the [Author.name] (in the very top-left corner of the [Embed]).
 * @property provider TODO Discord refuses to explain what this is
 * @property fields A [List] of all [Field]s in the [Embed] in order of appearance (top -> bottom, left -> right).
 * @property image The [EmbedGraphic] which is shown at the bottom of the embed as a large image.
 * @property video
 * @property color The color of the [Embed]'s left border. Leaving this `null` will result in the default greyish color.
 * @property footer The [Footer] of the embed shown at the very bottom.
 * @property timeStamp The timestamp is shown to the right of the [footer] and is usually used to mark when the embed
 * was sent, but can be set to any [DateTimeTz].
 */
@Serializable
data class Embed(
    val title: String? = "",
    val type: String? = null,
    val description: String? = "",
    @SerialName("url") val titleUrl: String? = null,
    @SerialName("timestamp") private val time_stamp: String? = null,
    @SerialName("color") private val color_int: Int? = null,
    val footer: Footer? = null,
    val image: EmbedGraphic? = null,
    val thumbnail: EmbedGraphic? = null,
    val video: EmbedGraphic? = null,
    val provider: Provider? = null, // No idea what this means
    val author: Author? = null,
    val fields: List<Field> = emptyList()
) {

    @Serializable
    data class Author(
        val name: String? = null,
        val url: String? = null,
        @SerialName("icon_url") val imgUrl: String? = null,
        @SerialName("proxy_icon_url") val proxyImgUrl: String? = null
    ) {
        init {
            if (name?.length !in 1..AUTHOR_NAME_MAX)
                throw IllegalArgumentException("Name must be within ${1..AUTHOR_NAME_MAX} char. (was ${name?.length})")
        }
    }

    @Serializable
    data class Provider(val name: String? = null, val url: String? = null)

    /**
     * A [Field] is a titled paragraph displayed in order under the [description].
     *
     * @property name The title of the [Field].
     * @property content The text displayed under the [name]
     * @property inline Whether the [Field] should be displayed inline (i.e., next to another [inline] [Field] where
     * possible).
     */
    @Serializable
    data class Field(val name: String, @SerialName("value") val content: String, val inline: Boolean = false) {
        init {
            if (name.length !in 1..FIELD_NAME_MAX)
                throw IllegalArgumentException("Name must be within ${1..FIELD_NAME_MAX} char. (was ${name.length})")
            if (content.length !in 1..FIELD_VAL_MAX)
                throw IllegalArgumentException(
                    "Content must be within ${1..FIELD_VAL_MAX} char. (was ${content.length})"
                )
        }
    }

    /** An image or video within the [Embed]. */
    @Serializable
    data class EmbedGraphic(
        val url: String? = null,
        @SerialName("proxy_url") val proxyImgUrl: String? = null,
        val height: Short? = null,
        val width: Short? = null
    )

    @Serializable
    data class Footer(
        val text: String?,
        @SerialName("icon_url") val imgUrl: String? = null,
        @SerialName("proxy_icon_url") val proxyImgUrl: String? = null
    ) {
        init {
            if (text?.length !in 1..FOOTER_MAX)
                throw IllegalArgumentException("Name must be within ${1..FOOTER_MAX} char. (was ${text?.length})")
        }
    }

    @Transient val timeStamp get() = time_stamp?.let{ DateFormat.ISO_WITHOUT_MS.parse(it) }
    @Transient val color: Color? get() = color_int?.let { Color(it) }

    init {
        if (title?.length !in 1..TITLE_MAX)
            throw IllegalArgumentException("Title must be within ${1..TITLE_MAX} char. (was ${title?.length})")
        if (description?.length !in 1..DESCRIPTION_MAX)
            throw IllegalArgumentException(
                "Description must be within ${1..DESCRIPTION_MAX} char. (was ${description?.length})"
            )
        if (fields.size > FIELD_MAX)
            throw IllegalArgumentException("Cannot have ${fields.size} fields. Max fields=$FIELD_MAX")
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("Author = $author\n")
            .append("Title = $title (url = $titleUrl)\n")
            .append("Description = $description\n")
            .append("Color = $color\n")
            .append("Thumbnail = $thumbnail\n")
            .append("Fields [\n")
        sb.append(fields.joinToString("") { "$it\n" })
            .append("]\nImage = $image\n")
            .append("Video = $video\n")
            .append("Footer = $footer\n")
            .append("Provider = $provider\n")
            //.append("Type = $type\n")

        return sb.toString()
    }

    companion object {
        const val TITLE_MAX = 256
        const val DESCRIPTION_MAX =	2048
        /** The maximum number of [fields][Field] a [Embed] can have. */
        const val FIELD_MAX = 25
        const val FIELD_NAME_MAX = 256
        const val FIELD_VAL_MAX = 1024
        const val FOOTER_MAX = 2048
        const val AUTHOR_NAME_MAX = 256
    }
}

/**
 * An [EmbedBuilder] is used to create a usable [Embed]. An [Embed] is a card-like content display sent by Webhooks
 * and Bots. [Here](https://imgur.com/a/yOb5n) you can see each part of the embed explained and shown.
 *
 * You can use an embed preview took [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 */
class EmbedBuilder(builder: EmbedBuilder.() -> Unit = {}) {

    /** The [title] of the embed appears atop the [description] and right below the [author]. */
    var title: String? = null
    /**
     * The description of the embed appears after the [title] and before any [Field]. The
     * [description] supports standard Discord markdown as well as [markdown\](links).
     */
    var description: String? = null
    /** The url which when the [title] is clicked will be opened. Set this to `null` for no link. */
    var titleUrl: String? = null
    /**
     * The author who's name will appear at the very top of the [Embed]. The [Author.imgUrl] will be
     * shown to the left of the [Author.name] (in the very top-left corner of the [Embed]).
     */
    var author: Author? = null
    /** The color of the [Embed]'s left border. Leaving this `null` will result in the default greyish color. */
    var color: Color? = null
    /** A [List] of all [Field]s in the [Embed] in order of appearance (top -> bottom, left -> right). */
    var fields: MutableList<Field> = mutableListOf()
    /** The [EmbedGraphic] which is shown at the bottom of the embed as a large image. */
    var image: EmbedGraphic? = null
    /**
     * The thumbnail appears in the upper-right-hand corner of the embed as a smaller image.
     * Set this to `null` for no thumbnail.
     */
    var thumbnail: EmbedGraphic? = null
    var video: EmbedGraphic? = null
    /** The [Footer] of the embed shown at the very bottom. */
    var footer: Embed.Footer? = null
    /**
     * The timestamp is shown to the right of the [footer] and is usually used
     * to mark when the embed was sent, but can be set to any [DateTimeTz].
     */
    var timestamp: DateTime? = null

    init {
        builder(this)
    }

    inner class TitleBuilder {
        /** The [title] of the embed appears atop the [description] and right below the [author]. */
        lateinit var title: String
        /** The url which when the [title] is clicked will be opened. Set this to `null` for no link. */
        var url: String? = null

        fun build() { this@EmbedBuilder.title = title; this@EmbedBuilder.titleUrl = url }
    }

    inner class AuthorBuilder {
        /**  The name will appear at the very top of the [Embed]. */
        var name: String? = null
        /** The hyper link embedded in the [name]. */
        var url: String? = null
        /** The [Author.imgUrl] will be shown to the left of the [name] (in the very top-left corner of the [Embed]). */
        var imgUrl: String? = null
        var proxyImgUrl: String? = null

        fun build(): Author = Author(name, url, imgUrl, proxyImgUrl)
    }

    inner class FieldBuilder(inline: Boolean = false) {
        /** The title of the [Field]. */
        var name: String = ""
        /** The text displayed under the [name]. */
        var content: String = ""
        /** Whether the [Field] should be displayed inline (i.e., next to another [inline] [Field] where possible). */
        var inline: Boolean = inline

        fun build(): Field = Field(name, content, inline)
    }

    /** An image or video within the [Embed]. */
    inner class GraphicBuilder {
        /** The URL of the image. */
        var url: String? = null
        var proxyImgUrl: String? = null

        fun build(): EmbedGraphic = EmbedGraphic(url, proxyImgUrl)
    }

    inner class FooterBuilder {
        /** The Text to be shown in the [Footer] */
        var text: String? = null
        /** The image to be shown to the left of the [text]. */
        var imgUrl: String? = null
        var proxyImgUrl: String? = null

        fun build(): Footer = Footer(text, imgUrl, proxyImgUrl)
    }

    /**
     * Use this function to set the [Embed.title] and [Embed.titleUrl].
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
    @StrifeDsl
    fun title(title: String? = null, titleUrl: String? = null, builder: TitleBuilder.() -> Unit = {}) {
        title?.let { this.title = it }
        titleUrl?.let { this.titleUrl = it }
        TitleBuilder().apply(builder).build()
    }

    /**
     * Use this function to set the [Embed.author].
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
    @StrifeDsl
    fun author(builder: AuthorBuilder.() -> Unit) {
        author = AuthorBuilder().also(builder).build()
    }


    /**
     * Use this function to add a [Field][Embed.Field].
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
    @StrifeDsl
    fun field(inline: Boolean = false, builder: FieldBuilder.() -> Unit) =
        fields.add(FieldBuilder(inline).apply(builder).build())

    /**
     * Use this function to add a [Field][Embed.Field].
     *
     * This function is used in DSL form.
     * ```kotlin
     *     field(name = String, inline = Boolean) {
     *          "content"
     *     }
     * ```
     */
    @StrifeDsl
    fun field(name: String, inline: Boolean = false, content: () -> String) = field(inline) {
        this.name = name
        this.content = content()
    }

    /**
     * Use this function to add a [inline][Field.inline] [Field][Embed.Field].
     *
     * This function is used in DSL form.
     * ```kotlin
     *     inlineField {
     *          name = "NAME"
     *          content = "CONTENT"
     *     }
     * ```
     */
    @StrifeDsl
    fun inlineField(builder: FieldBuilder.() -> Unit) = field(true, builder)

    /**
     * Use this function to add a [inline][Field.inline] [Field][Embed.Field].
     *
     * This function is used in DSL form.
     * ```kotlin
     *     inlineField(name = "NAME") {
     *          "content"
     *     }
     * ```
     */
    @StrifeDsl
    fun inlineField(name: String, content: () -> String) = field(true) {
        this.name = name
        this.content = content()
    }

    /** Set the [thumbnail image][Embed.thumbnail]. */
    @StrifeDsl
    fun thumbnail(url: String? = null, builder: GraphicBuilder.() -> Unit = {}) {
        thumbnail = GraphicBuilder().apply { this.url = url }.apply(builder).build()
    }

    /** Set the [image][Embed.image]. */
    @StrifeDsl
    fun image(url: String? = null, builder: GraphicBuilder.() -> Unit = {}) {
        image = GraphicBuilder().apply { this.url = url }.apply(builder).build()
    }

    /** Set the [video][Embed.video]. */
    @StrifeDsl
    fun video(url: String? = null, builder: GraphicBuilder.() -> Unit = {}) {
        video = GraphicBuilder().apply { this.url = url }.apply(builder).build()
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
    @StrifeDsl
    fun footer(builder: FooterBuilder.() -> Unit) {
        footer = FooterBuilder().apply(builder).build()
    }

    /** Build the [EmbedBuilder] into a usable [Embed]. */
    fun build() = Embed(
        title = title,
        description = description,
        titleUrl = titleUrl,
        time_stamp = timestamp?.format(DateFormat.ISO_WITH_MS),
        color_int = color?.rgb,
        footer = footer,
        image = image,
        thumbnail = thumbnail,
        video = video,
        author = author,
        fields = fields
    )
}

/**
 * Build an [Embed] in a type-safe DSL function.
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
@StrifeDsl
fun embed(builder: EmbedBuilder.() -> Unit): Embed = EmbedBuilder().apply(builder).build()

/** Convert the [EmbedPacket] to an [Embed]. */
internal fun EmbedPacket.toEmbed() = Embed(
        title,
    null,
        description,
        url,
        timestamp,
        color,
        Footer(footer?.text, footer?.icon_url, footer?.proxy_icon_url),
        EmbedGraphic(image?.url, image?.proxy_url, image?.height, image?.width),
        EmbedGraphic(thumbnail?.url, thumbnail?.proxy_url, thumbnail?.height, thumbnail?.width),
        EmbedGraphic(video?.url, video?.proxy_url, video?.height, video?.width),
        Provider(provider?.name, provider?.url),
        Author(author?.name, author?.url, author?.icon_url, author?.proxy_icon_url),
        fields?.map { Field(it.name, it.value, it.inline ?: false) } ?: emptyList()
    )
