package com.serebit.diskord.entities

import com.serebit.diskord.EntityCache
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.AttachmentData
import com.serebit.diskord.data.EmbedData
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.network.ApiEndpoint
import com.serebit.diskord.network.ApiRequester
import kotlinx.coroutines.experimental.runBlocking
import java.time.OffsetDateTime

class Message internal constructor(
    override val id: Snowflake,
    val author: User,
    channel_id: Snowflake,
    content: String,
    timestamp: IsoTimestamp,
    edited_timestamp: IsoTimestamp?,
    tts: Boolean,
    mention_everyone: Boolean,
    mentions: List<User>,
    mention_roles: List<Role>,
    attachments: Array<AttachmentData>,
    embeds: Array<EmbedData>,
    pinned: Boolean,
    type: Int
) : DiscordEntity {
    val channel: TextChannel = EntityCache.find(channel_id)
        ?: runBlocking { ApiRequester.requestObject(ApiEndpoint.getChannel(channel_id)).await() as TextChannel }
    val createdAt: OffsetDateTime = OffsetDateTime.parse(timestamp)
    var content: String = content
        private set
    var editedAt: OffsetDateTime? = edited_timestamp?.let { OffsetDateTime.parse(it) }
        private set
    var userMentions: List<User> = mentions
        private set
    var roleMentions: List<Role> = mention_roles
        private set
    var mentionsEveryone: Boolean = mention_everyone
        private set
    var isPinned: Boolean = pinned
        private set
    var isTextToSpeech = tts
        private set

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
}