package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.ISO_WITHOUT_MS
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse

internal class MessageData(
    packet: MessageCreatePacket,
    val channel: TextChannelData<*, *>,
    override val context: BotClient
) : EntityData<PartialMessagePacket, Message> {
    override val id = packet.id
    override val lazyEntity by lazy { Message(this) }

    val guild = packet.guild_id?.let { context.cache.getGuildData(it) }
    val author = context.cache.pullUserData(packet.author)
    val member = packet.member
    var content = packet.content
    var createdAt = try {
        DateFormat.ISO_WITH_MS.parse(packet.timestamp)
    } catch (ex: Exception) {
        DateFormat.ISO_WITHOUT_MS.parse(packet.timestamp)
    }
    var editedAt = packet.edited_timestamp?.let { DateFormat.ISO_WITH_MS.parse(it) }
    val isTextToSpeech = packet.tts
    var mentionsEveryone = packet.mention_everyone
    var mentionedUsers = packet.mentions.mapNotNull { context.cache.getUserData(it.id) }
    var mentionedRoles = packet.mention_roles.mapNotNull { guild!!.getRoleData(it) }
    var attachments = packet.attachments
    var embeds = packet.embeds
    var reactions = packet.reactions
    val nonce = packet.nonce
    var isPinned = packet.pinned
    val webhookID = packet.webhook_id
    val type = packet.type
    val activity = packet.activity
    val application = packet.application

    override fun update(packet: PartialMessagePacket) {
        packet.content?.let { content = it }
        packet.edited_timestamp?.let { editedAt = DateFormat.ISO_WITH_MS.parse(it) }
        packet.mention_everyone?.let { mentionsEveryone = it }
        packet.mentions?.let { users ->
            mentionedUsers = users.map { context.cache.pullUserData(it) }
        }
        packet.mention_roles?.let { ids -> mentionedRoles = ids.mapNotNull { guild!!.getRoleData(it) } }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }
}

internal fun MessageCreatePacket.toData(channel: TextChannelData<*, *>, context: BotClient) =
    MessageData(this, channel, context)
