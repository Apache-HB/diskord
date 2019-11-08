package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class MessageCreatePacket(
    override val id: Long,
    val channel_id: Long,
    val guild_id: Long? = null,
    val author: UserPacket,
    val member: PartialMemberPacket? = null,
    val content: String,
    val timestamp: String,
    val edited_timestamp: String?,
    val tts: Boolean,
    val mention_everyone: Boolean,
    val mentions: Set<UserPacket>,
    val mention_roles: Set<Long>,
    val attachments: List<AttachmentPacket>,
    val embeds: List<EmbedPacket>,
    val reactions: List<ReactionPacket> = emptyList(),
    val nonce: Long? = null,
    val pinned: Boolean,
    val webhook_id: Long? = null,
    val type: Byte,
    val activity: ActivityPacket? = null,
    val application: ApplicationPacket? = null
) : EntityPacket

@Serializable
internal data class PartialMessagePacket(
    override val id: Long,
    val channel_id: Long,
    val guild_id: Long? = null,
    val author: UserPacket? = null,
    val member: PartialMemberPacket? = null,
    val content: String? = null,
    val timestamp: String? = null,
    val edited_timestamp: String? = null,
    val tts: Boolean? = null,
    val mention_everyone: Boolean? = null,
    val mentions: Set<UserPacket>? = null,
    val mention_roles: Set<Long>? = null,
    val attachments: List<AttachmentPacket>? = null,
    val embeds: List<EmbedPacket>? = null,
    val reactions: List<ReactionPacket>? = null,
    val nonce: Long? = null,
    val pinned: Boolean? = null,
    val webhook_id: Long? = null,
    val type: Byte? = null,
    val activity: ActivityPacket? = null,
    val application: ApplicationPacket? = null
) : EntityPacket

@Serializable
internal data class ReactionPacket(val count: Int, val me: Boolean, val emoji: PartialEmojiPacket)

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
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val url: String? = null,
    val timestamp: String? = null,
    val color: Int? = null,
    val footer: FooterData? = null,
    val image: ImageData? = null,
    val thumbnail: ThumbnailData? = null,
    val video: VideoData? = null,
    val provider: ProviderData? = null,
    val author: AuthorData? = null,
    val fields: List<FieldData>? = null
) {
    @Serializable
    data class ThumbnailData(
        val url: String? = null,
        val proxy_url: String? = null,
        val height: Short? = null,
        val width: Short? = null
    )

    @Serializable
    data class VideoData(
        val url: String? = null,
        val proxy_url: String? = null,
        val height: Short? = null,
        val width: Short? = null
    )

    @Serializable
    data class ImageData(
        val url: String? = null,
        val proxy_url: String? = null,
        val height: Short? = null,
        val width: Short? = null
    )

    @Serializable
    data class ProviderData(val name: String? = null, val url: String? = null)

    @Serializable
    data class AuthorData(
        val name: String? = null,
        val url: String? = null,
        val icon_url: String? = null,
        val proxy_icon_url: String? = null
    )

    @Serializable
    data class FooterData(val text: String, val icon_url: String? = null, val proxy_icon_url: String? = null)

    @Serializable
    data class FieldData(val name: String, val value: String, val inline: Boolean? = false)
}

@Serializable
internal data class AttachmentPacket(
    val id: Long,
    val filename: String,
    val size: Int,
    val url: String,
    val proxy_url: String,
    val height: Short? = null,
    val width: Short? = null
)
