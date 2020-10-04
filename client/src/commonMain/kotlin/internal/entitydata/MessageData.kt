package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.GetCacheData
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.parseSafe
import kotlinx.datetime.Instant

internal class MessageData(
    packet: MessageCreatePacket,
    override val context: BotClient
) : EntityData<PartialMessagePacket, Message> {
    override val id = packet.id
    override val lazyEntity by lazy { Message(id, channelID, context) }
    val channelID = packet.channel_id
    val guildID = packet.guild_id
    val channelType = packet.guild_id?.let { ChannelType.GUILD } ?: ChannelType.DM
    suspend fun getGuild() = guildID?.let { context.cache.getGuildData(it) }
    suspend fun getChannel() = when (channelType) {
        ChannelType.DM -> context.obtainDmChannelData(channelID)
        ChannelType.GUILD -> context.obtainGuildTextChannelData(channelID)
    }

    val member = packet.member?.toMemberPacket(packet.author, packet.guild_id!!)
    val author = member?.user?.toData(context) ?: context.cache.pullUserData(packet.author)
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
    var mentionedRoles = packet.mention_roles.mapNotNull { context.cache.get(GetCacheData.GuildRole(it)) }
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
        packet.mention_roles?.let { ids ->
            mentionedRoles = ids.mapNotNull { context.cache.get(GetCacheData.GuildRole(it)) }
        }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }
}

internal fun MessageCreatePacket.toData(context: BotClient) = MessageData(this, context)

private fun PartialMemberPacket.toMemberPacket(user: UserPacket, guildID: Long) =
    GuildMemberPacket(user, nick, guildID, roles, joined_at!!, deaf, mute)
