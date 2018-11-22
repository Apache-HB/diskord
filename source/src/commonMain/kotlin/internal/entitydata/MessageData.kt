package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket

internal class MessageData(packet: MessageCreatePacket, override val context: Context) : EntityData {
    override val id = packet.id
    val channel = context.cache.findChannel<TextChannelData>(packet.channel_id)!!
    val guild = context.cache.guilds[packet.guild_id]
    val author = context.cache.users[packet.author.id]!!
    val member = packet.member
    var content = packet.content
    var createdAt = packet.timestamp.toDateTime()
    var editedAt = packet.edited_timestamp?.toDateTime()
    val isTextToSpeech = packet.tts
    var mentionsEveryone = packet.mention_everyone
    var mentionedUsers = packet.mentions.mapNotNull { context.cache.users[it.id] }
    var mentionedRoles = packet.mention_roles.mapNotNull { context.cache.findRole(it) }
    var attachments = packet.attachments
    var embeds = packet.embeds
    var reactions = packet.reactions
    val nonce = packet.nonce
    var isPinned = packet.pinned
    val webhookId = packet.webhook_id
    val type = packet.type
    val activity = packet.activity
    val application = packet.application

    fun update(packet: PartialMessagePacket) = apply {
        packet.content?.let { content = it }
        packet.edited_timestamp?.let { editedAt = it.toDateTime() }
        packet.mention_everyone?.let { mentionsEveryone = it }
        packet.mentions?.let { users -> mentionedUsers = users.mapNotNull { context.cache.users[it.id] } }
        packet.mention_roles?.let { roleIds -> mentionedRoles = roleIds.mapNotNull { context.cache.findRole(it) } }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }
}
