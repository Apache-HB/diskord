package com.serebit.diskord.data

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake

data class MessageData(
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
}
