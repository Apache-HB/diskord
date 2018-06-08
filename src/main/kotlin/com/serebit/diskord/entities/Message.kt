package com.serebit.diskord.entities

import com.serebit.diskord.EntityCache
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake

class Message internal constructor(
    override val id: Snowflake,
    val author: User,
    channel_id: Snowflake,
    val content: String,
    timestamp: IsoTimestamp,
    edited_timestamp: IsoTimestamp?,
    tts: Boolean,
    mention_everyone: Boolean,
    mentions: Array<User>,
    mention_roles: Array<Role>,
    attachments: Array<AttachmentData>,
    embeds: Array<EmbedData>,
    pinned: Boolean,
    type: Int
) : DiscordEntity {
    val channel: TextChannel = EntityCache.find(channel_id)!!

    init {
        EntityCache.cache(this)
    }

    fun reply(text: String) = channel.send(text)

    enum class MessageType(val value: Int) {
        DEFAULT(0),
        RECIPIENT_ADD(1), RECIPIENT_REMOVE(2),
        CALL(3),
        CHANNEL_NAME_CHANGE(4), CHANNEL_ICON_CHANGE(5), CHANNEL_PINNED_MESSAGE(6),
        GUILD_MEMBER_JOIN(7)
    }

    data class AttachmentData(
        val id: Snowflake,
        val filename: String,
        val size: Int,
        val url: String,
        val proxy_url: String,
        val height: Int?,
        val width: Int?
    )

    data class EmbedData(
        val title: String?,
        val type: String?,
        val description: String?,
        val url: String?,
        val timestamp: IsoTimestamp?,
        val color: Int?,
        val footer: FooterData?,
        val image: ImageData?,
        val thumbnail: ThumbnailData?,
        val video: VideoData?,
        val provider: ProviderData?,
        val author: AuthorData?,
        val fields: List<FieldData>?
    ) {
        data class ThumbnailData(
            val url: String?,
            val proxy_url: String?,
            val height: Int?,
            val width: Int?
        )

        data class VideoData(
            val url: String?,
            val proxy_url: String?,
            val height: Int?,
            val width: Int?
        )

        data class ImageData(
            val url: String?,
            val proxy_url: String?,
            val height: Int?,
            val width: Int?
        )

        data class ProviderData(
            val name: String?,
            val url: String?
        )

        data class AuthorData(
            val name: String?,
            val url: String?,
            val icon_url: String?,
            val proxy_icon_url: String?
        )

        data class FooterData(
            val text: String,
            val icon_url: String?,
            val proxy_icon_url: String?
        )

        data class FieldData(
            val name: String,
            val value: String,
            val inline: Boolean?
        )
    }
}
