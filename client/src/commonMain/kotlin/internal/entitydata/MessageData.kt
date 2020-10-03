package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.parseSafe
import kotlinx.datetime.Instant

internal class MessageData(
    packet: MessageCreatePacket,
    val channel: TextChannelData<*, *>,
    override val context: BotClient
) : EntityData<PartialMessagePacket, Message> {
    override val id = packet.id
    override val lazyEntity by lazy { Message(this) }
    val guild = (channel as? GuildChannelData<*, *>)?.guild
    val member = packet.member?.toMemberPacket(packet.author, packet.guild_id!!)?.let { guild!!.update(it) }
    val author = member?.user ?: context.cache.pullUserData(packet.author)
    val type = Message.Type.values()[packet.type.toInt()]
    val nonce = packet.nonce
    val webhookID = packet.webhook_id
    val activity = packet.activity
    val application = packet.application
    val isTextToSpeech = packet.tts
    val createdAt = Instant.parseSafe(packet.timestamp)
    var editedAt = packet.edited_timestamp?.let { Instant.parseSafe(it) }
        private set
    var content = packet.content
        private set
    var mentionsEveryoneOrHere = packet.mention_everyone
        private set
    var mentionedUsers = packet.mentions.map { context.cache.pullUserData(it) }
        private set
    var mentionedRoles = packet.mention_roles.mapNotNull { guild!!.getRoleData(it) }
        private set
    var attachments = packet.attachments
        private set
    var embeds = packet.embeds
        private set
    var reactions = packet.reactions
        private set
    var isPinned = packet.pinned
        private set

    override fun update(packet: PartialMessagePacket) {
        packet.content?.let { content = it }
        packet.edited_timestamp?.let { editedAt = Instant.parseSafe(it) }
        packet.mention_everyone?.let { mentionsEveryoneOrHere = it }
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

private fun PartialMemberPacket.toMemberPacket(user: UserPacket, guildID: Long) =
    GuildMemberPacket(user, nick, guildID, roles, joined_at!!, deaf, mute)
