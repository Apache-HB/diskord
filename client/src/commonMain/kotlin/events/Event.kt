package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.dispatches.*
import kotlinx.serialization.KSerializer

/**
 * An event corresponds to data sent by the Discord API regarding some action or change upon data the bot client has
 * access to.
 */
interface Event {
    /** The [BotClient] in which this [Event] took place. */
    val context: BotClient
}

private fun TODO() = null

/**
 * A Gateway Event defined by the
 * [Discord API docs](https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events).
 * The [Enum.name] is the exact name of the event sent by Discord, and each instance includes both a short
 * [description] of the event type and the [serializer] used to convert the raw JSON to a Kotlin object.
 */
@Suppress("UNUSED")
internal enum class EventName(val description: String, val serializer: KSerializer<out DispatchPayload>?) {
    READY("contains the initial state information", Ready.serializer()),
    RESUMED("client has resumed", Resumed.serializer()),
    CHANNEL_CREATE("new channel created", ChannelCreate.serializer()),
    CHANNEL_UPDATE("channel was updated", ChannelUpdate.serializer()),
    CHANNEL_DELETE("channel was deleted", ChannelDelete.serializer()),
    CHANNEL_PINS_UPDATE("message was pinned or unpinned", ChannelPinsUpdate.serializer()),
    GUILD_CREATE(
        "lazy-load for unavailable guild, guild became available, or user joined a new guild",
        GuildCreate.serializer()
    ),
    GUILD_UPDATE("guild was updated", GuildUpdate.serializer()),
    GUILD_DELETE(
        "guild became unavailable, or user left/was removed from a guild", GuildDelete.serializer()
    ),
    GUILD_BAN_ADD("user was banned from a guild", GuildBanAdd.serializer()),
    GUILD_BAN_REMOVE("user was unbanned from a guild", GuildBanRemove.serializer()),
    GUILD_EMOJIS_UPDATE("guild emoji were updated", GuildEmojisUpdate.serializer()),
    GUILD_INTEGRATIONS_UPDATE("guild integration was updated", GuildIntegrationsUpdate.serializer()),
    GUILD_MEMBER_ADD("new user joined a guild", GuildMemberAdd.serializer()),
    GUILD_MEMBER_REMOVE("user was removed from a guild", GuildMemberRemove.serializer()),
    GUILD_MEMBER_UPDATE("guild member was updated", GuildMemberUpdate.serializer()),
    GUILD_MEMBERS_CHUNK("response to Request guild members", GuildMembersChunk.serializer()),
    GUILD_ROLE_CREATE("guild role was created", TODO()),
    GUILD_ROLE_UPDATE("guild role was updated", TODO()),
    GUILD_ROLE_DELETE("guild role was deleted", TODO()),
    MESSAGE_CREATE("message was created", MessageCreate.serializer()),
    MESSAGE_UPDATE("message was edited", MessageUpdate.serializer()),
    MESSAGE_DELETE("message was deleted", MessageDelete.serializer()),
    MESSAGE_DELETE_BULK("multiple messages were deleted at once", TODO()),
    MESSAGE_REACTION_ADD("user reacted to a message", MessageReactionAdd.serializer()),
    MESSAGE_REACTION_REMOVE("user removed a reaction from a message", MessageReactionRemove.serializer()),
    MESSAGE_REACTION_REMOVE_ALL(
        "all reactions were explicitly removed from a message", MessageReactionRemoveAll.serializer()
    ),
    PRESENCE_UPDATE("user was updated", PresenceUpdate.serializer()),
    TYPING_START("user started typing in a channel", TypingStart.serializer()),
    USER_UPDATE("properties about the user changed", TODO()),
    VOICE_STATE_UPDATE("someone joined, left, or moved a voice channel", TODO()),
    VOICE_SERVER_UPDATE("guild's voice server was updated", TODO()),
    WEBHOOKS_UPDATE("guild channel webhook was created, update, or deleted", TODO());

    companion object {
        /** Alternative to [valueOf] that returns null if the [name] doesn't exist in the values */
        fun byName(name: String) = values().find { it.name == name }
    }
}
