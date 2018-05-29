package com.serebit.diskord.data

import com.serebit.diskord.EntityCacher
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.TextChannel

internal data class MessageData(
    val id: Snowflake,
    val channel_id: Snowflake,
    val author: UserData,
    val content: String,
    val timestamp: IsoTimestamp,
    val edited_timestamp: IsoTimestamp?,
    val tts: Boolean,
    val mention_everyone: Boolean,
    val mentions: List<UserData>,
    val mention_roles: List<RoleData>,
    val attachments: List<AttachmentData>,
    val embeds: List<EmbedData>,
    val pinned: Boolean,
    val type: Int
) {
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
        val provider: ProviderData,
        val author: AuthorData,
        val fields: List<FieldData>
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

    fun toMessage() = EntityCacher.cache(Message(
        id.toLong(),
        EntityCacher.findChannel(channel_id)!! as TextChannel,
        EntityCacher.findUser(id)!!,
        content
    ))
}
