package com.serebit.diskord.internal.packets

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.cacheAll

internal data class MessagePacket(
    override val id: Long,
    private val author: UserPacket,
    val channel_id: Long,
    val content: String,
    private val timestamp: IsoTimestamp,
    private val edited_timestamp: IsoTimestamp?,
    val tts: Boolean,
    val mention_everyone: Boolean,
    private val mentions: Set<UserPacket>,
    private val mention_roles: Set<RolePacket>,
    val attachments: List<AttachmentPacket>,
    val embeds: List<EmbedPacket>,
    val pinned: Boolean,
    val type: Int
) : EntityPacket {
    val authorObj by lazy { User(author.id) }
    val channel
        get() = TextChannel.find(channel_id)
            ?: throw EntityNotFoundException("No channel with ID $channel_id found.")
    val timestampObj by lazy { DateTime.fromIsoTimestamp(timestamp) }
    val editedTimestamp by lazy { edited_timestamp?.let { DateTime.fromIsoTimestamp(it) } }
    val userMentions by lazy { mentions.map { User(it.id) } }
    val roleMentions by lazy { mention_roles.map { Role(it.id) } }

    init {
        author.cache()
        mentions.cacheAll()
        mention_roles.cacheAll()
    }
}

internal data class EmbedPacket(
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

internal data class AttachmentPacket(
    val id: Long,
    val filename: String,
    val size: Int,
    val url: String,
    val proxy_url: String,
    val height: Int?,
    val width: Int?
)

internal data class EmotePacket(
    val id: Long?,
    val name: String,
    val roles: List<Long>,
    val user: UserPacket?,
    val require_colons: Boolean?,
    val managed: Boolean?,
    val animated: Boolean?
)
