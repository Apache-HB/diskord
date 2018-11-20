package com.serebit.diskord.internal.packets

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.data.DateTime
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class MessageCreatePacket(
    override val id: Long,
    val channel_id: Long,
    @Optional val guild_id: Long? = null,
    val author: UserPacket,
    @Optional val member: PartialMemberPacket? = null,
    val content: String,
    val timestamp: IsoTimestamp,
    val edited_timestamp: IsoTimestamp?,
    val tts: Boolean,
    val mention_everyone: Boolean,
    val mentions: Set<UserPacket>,
    val mention_roles: Set<Long>,
    val attachments: List<AttachmentPacket>,
    val embeds: List<EmbedPacket>,
    @Optional val reactions: List<ReactionPacket> = emptyList(),
    @Optional val nonce: Long? = null,
    val pinned: Boolean,
    @Optional val webhook_id: Long? = null,
    val type: Int,
    @Optional val activity: ActivityPacket? = null,
    @Optional val application: ApplicationPacket? = null
) : EntityPacket {
    @Transient
    val timestampObj by lazy { DateTime.fromIsoTimestamp(timestamp) }
    @Transient
    val editedTimestamp by lazy { edited_timestamp?.let { DateTime.fromIsoTimestamp(it) } }
}

@Serializable
internal data class PartialMessagePacket(
    override val id: Long,
    val channel_id: Long,
    @Optional val guild_id: Long? = null,
    @Optional val author: UserPacket? = null,
    @Optional val member: PartialMemberPacket? = null,
    @Optional val content: String? = null,
    @Optional val timestamp: IsoTimestamp? = null,
    @Optional val edited_timestamp: IsoTimestamp? = null,
    @Optional val tts: Boolean? = null,
    @Optional val mention_everyone: Boolean? = null,
    @Optional val mentions: Set<UserPacket>? = null,
    @Optional val mention_roles: Set<Long>? = null,
    @Optional val attachments: List<AttachmentPacket>? = null,
    @Optional val embeds: List<EmbedPacket>? = null,
    @Optional val reactions: List<ReactionPacket>? = null,
    @Optional val nonce: Long? = null,
    @Optional val pinned: Boolean? = null,
    @Optional val webhook_id: Long? = null,
    @Optional val type: Int? = null,
    @Optional val activity: ActivityPacket? = null,
    @Optional val application: ApplicationPacket? = null
) : EntityPacket

@Serializable
internal data class ReactionPacket(
    val count: Int,
    val me: Boolean,
    val emoji: EmotePacket
)

@Serializable
internal data class ApplicationPacket(
    val id: Long,
    val cover_image: String,
    val description: String,
    val icon: String,
    val name: String
)

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
