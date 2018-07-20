package com.serebit.diskord.data

import com.serebit.diskord.IsoTimestamp

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
