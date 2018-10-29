package com.serebit.diskord.internal.packets

import com.serebit.diskord.IsoTimestamp

internal data class MessagePacket(
    override val id: Long,
    val author: UserPacket,
    val channel_id: Long,
    val content: String,
    val timestamp: IsoTimestamp,
    val edited_timestamp: IsoTimestamp?,
    val tts: Boolean,
    val mention_everyone: Boolean,
    val mentions: Set<UserPacket>,
    val mention_roles: Set<RolePacket>,
    val attachments: List<AttachmentPacket>,
    val embeds: List<EmbedPacket>,
    val pinned: Boolean,
    val type: Int
) : EntityPacket

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
