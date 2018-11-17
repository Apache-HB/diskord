package com.serebit.diskord.internal.packets

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.cacheAll
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class MessagePacket(
    override val id: Long,
    private val author: UserPacket,
    val channel_id: Long,
    val content: String,
    private val timestamp: IsoTimestamp,
    @Optional private val edited_timestamp: IsoTimestamp? = null,
    val tts: Boolean,
    val mention_everyone: Boolean,
    private val mentions: Set<UserPacket>,
    private val mention_roles: Set<RolePacket>,
    val attachments: List<AttachmentPacket>,
    val embeds: List<EmbedPacket>,
    val pinned: Boolean,
    val type: Int
) : EntityPacket {
    @Transient
    val authorObj by lazy { User(author.id) }
    @Transient
    val channel
        get() = TextChannel.find(channel_id)
            ?: throw EntityNotFoundException("No channel with ID $channel_id found.")
    @Transient
    val timestampObj by lazy { DateTime.fromIsoTimestamp(timestamp) }
    @Transient
    val editedTimestamp by lazy { edited_timestamp?.let { DateTime.fromIsoTimestamp(it) } }
    @Transient
    val userMentions by lazy { mentions.map { User(it.id) } }
    @Transient
    val roleMentions by lazy { mention_roles.map { Role(it.id) } }

    init {
        author.cache()
        mentions.cacheAll()
        mention_roles.cacheAll()
    }
}

@Serializable
internal data class EmbedPacket(
    @Optional val title: String? = null,
    @Optional val type: String? = null,
    @Optional val description: String? = null,
    @Optional val url: String? = null,
    @Optional val timestamp: IsoTimestamp? = null,
    @Optional val color: Int? = null,
    @Optional val footer: FooterData? = null,
    @Optional val image: ImageData? = null,
    @Optional val thumbnail: ThumbnailData? = null,
    @Optional val video: VideoData? = null,
    @Optional val provider: ProviderData? = null,
    @Optional val author: AuthorData? = null,
    @Optional val fields: List<FieldData>? = null
) {
    @Serializable
    data class ThumbnailData(
        @Optional val url: String? = null,
        @Optional val proxy_url: String? = null,
        @Optional val height: Int? = null,
        @Optional val width: Int? = null
    )

    @Serializable
    data class VideoData(
        @Optional val url: String? = null,
        @Optional val proxy_url: String? = null,
        @Optional val height: Int? = null,
        @Optional val width: Int? = null
    )

    @Serializable
    data class ImageData(
        @Optional val url: String? = null,
        @Optional val proxy_url: String? = null,
        @Optional val height: Int? = null,
        @Optional val width: Int? = null
    )

    @Serializable
    data class ProviderData(
        @Optional val name: String? = null,
        @Optional val url: String? = null
    )

    @Serializable
    data class AuthorData(
        @Optional val name: String? = null,
        @Optional val url: String? = null,
        @Optional val icon_url: String? = null,
        @Optional val proxy_icon_url: String? = null
    )

    @Serializable
    data class FooterData(
        val text: String,
        @Optional val icon_url: String? = null,
        @Optional val proxy_icon_url: String? = null
    )

    @Serializable
    data class FieldData(
        val name: String,
        val value: String,
        @Optional val inline: Boolean? = false
    )
}

@Serializable
internal data class AttachmentPacket(
    val id: Long,
    val filename: String,
    val size: Int,
    val url: String,
    val proxy_url: String,
    @Optional val height: Int? = null,
    @Optional val width: Int? = null
)

@Serializable
internal data class EmotePacket(
    val id: Long?,
    val name: String,
    val roles: List<Long>,
    @Optional val user: UserPacket? = null,
    @Optional val require_colons: Boolean? = null,
    @Optional val managed: Boolean? = null,
    @Optional val animated: Boolean? = null
)
