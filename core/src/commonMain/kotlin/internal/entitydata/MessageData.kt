package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse

internal class MessageData(
    packet: MessageCreatePacket, override val context: Context
) : EntityData<PartialMessagePacket, Message> {
    override val id = packet.id
    val channel = context.cache.getTextChannelData(packet.channel_id)!!
    val guild = packet.guild_id?.let { context.cache.getGuildData(it) }
    val author = context.cache.pullUserData(packet.author)
    val member = packet.member
    var content = packet.content
    var createdAt = DateFormat.ISO_WITH_MS.parse(packet.timestamp)
    var editedAt = packet.edited_timestamp?.let { DateFormat.ISO_WITH_MS.parse(it) }
    val isTextToSpeech = packet.tts
    var mentionsEveryone = packet.mention_everyone
    var mentionedUsers = packet.mentions.mapNotNull { context.cache.getUserData(it.id) }
    var mentionedRoles = packet.mention_roles.mapNotNull { guild!!.roles[it] }
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
        packet.mention_roles?.let { ids -> mentionedRoles = ids.mapNotNull { guild!!.roles[it] } }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }

    override fun toEntity() = Message(this)
}

internal fun MessageCreatePacket.toData(context: Context) = MessageData(this, context)
