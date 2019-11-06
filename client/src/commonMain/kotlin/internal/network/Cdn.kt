package com.serebit.strife.internal.network

/**
 * A class containing all Discord CDN endpoints for image formatting. Each endpoint has a [path] formatted in a
 * specific way according to [Discord docs][https://discordapp.com/developers/docs/reference#image-formatting], and
 * supports some [ImageFormats][ImageFormat] that can be provided using [format] field. The result is a formatted
 * String by the [toString] method.
 */
internal sealed class Cdn(private val path: String, private val format: ImageFormat) {
    class CustomEmoji(emojiID: Long, format: ImageFormat) : Cdn("emojis/$emojiID", format)

    class GuildIcon(guildID: Long, guildIcon: String, format: ImageFormat) :
        Cdn("icons/$guildID/$guildIcon", format)

    class GuildSplash(guildID: Long, guildSplash: String, format: ImageFormat) :
        Cdn("splashes/$guildID/$guildSplash", format)

    class GuildBanner(guildID: Long, guildBanner: String, format: ImageFormat) :
        Cdn("banners/$guildID/$guildBanner", format)

    class DefaultUserAvatar(userDiscriminator: Byte) : Cdn("embed/avatars/$userDiscriminator", ImageFormat.Png)

    class UserAvatar(userID: Long, userAvatar: String, format: ImageFormat) :
        Cdn("avatars/$userID/${if (format == ImageFormat.Gif) "a_" else ""}$userAvatar", format)

    class ApplicationIcon(applicationID: Long, icon: String, format: ImageFormat) :
        Cdn("app-icons/$applicationID/$icon", format)

    class ApplicationAsset(applicationID: Long, assetID: String, format: ImageFormat) :
        Cdn("app-assets/$applicationID/$assetID", format)

    override fun toString() = "$baseUri$path.${format.name.toLowerCase()}"

    companion object {
        private const val baseUri = "https://cdn.discordapp.com/"
    }
}

/** Supported image formats by Discord CDN endpoints. */
internal enum class ImageFormat { Jpg, Png, WebP, Gif }
