package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.dispatches.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer

// TODO update cache data on events

/**
 * An event is an action sent by the Discord API regarding some action or change upon data
 * the bot client has access to. A full list of events can be seen [here][EventName].
 */
interface Event {
    val context: Context
}

private fun TODO() = Ready.serializer() // TODO delete when all events are impl

/**
 * A Gateway Event defined by the
 * [Discord API docs](https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events).
 * The [name][Enum.name] is the exact name of the event sent by Discord
 *
 * @property description The description of the event provided by Discord
 * @property serializer The [payload][DispatchPayload] [serializer][Serializer] for this event
 */
enum class EventName(
    val description: String,
    internal val serializer: KSerializer<out DispatchPayload>
) {
    READY("contains the initial state information", Ready.serializer()),
    CHANNEL_CREATE("new channel created", ChannelCreate.serializer()),
    CHANNEL_UPDATE("channel was updated", ChannelUpdate.serializer()),
    CHANNEL_DELETE("channel was deleted", ChannelDelete.serializer()),
    CHANNEL_PINS_UPDATE("message was pinned or unpinned", ChannelPinsUpdate.serializer()),
    GUILD_CREATE(
        "lazy-load for unavailable guild, guild became available, or user joined a new guild",
        GuildCreate.serializer()
    ),
    GUILD_UPDATE("guild was updated", GuildUpdate.serializer()),
    GUILD_DELETE("guild became unavailable, or user left/was removed from a guild", GuildDelete.serializer()),
    GUILD_BAN_ADD("user was banned from a guild", TODO()),
    GUILD_BAN_REMOVE("user was unbanned from a guild", TODO()),
    GUILD_EMOJIS_UPDATE("guild emoji were updated", TODO()),
    GUILD_INTEGRATIONS_UPDATE("guild integration was updated", TODO()),
    GUILD_MEMBER_ADD("new user joined a guild", TODO()),
    GUILD_MEMBER_REMOVE("user was removed from a guild", TODO()),
    GUILD_MEMBER_UPDATE("guild member was updated", TODO()),
    GUILD_MEMBERS_CHUNK("response to Request guild members", TODO()),
    GUILD_ROLE_CREATE("guild role was created", TODO()),
    GUILD_ROLE_UPDATE("guild role was updated", TODO()),
    GUILD_ROLE_DELETE("guild role was deleted", TODO()),
    MESSAGE_CREATE("message was created", MessageCreate.serializer()),
    MESSAGE_UPDATE("message was edited", MessageUpdate.serializer()),
    MESSAGE_DELETE("message was deleted", MessageDelete.serializer()),
    MESSAGE_DELETE_BULK("multiple messages were deleted at once", TODO()),
    MESSAGE_REACTION_ADD("user reacted to a message", TODO()),
    MESSAGE_REACTION_REMOVE("user removed a reaction from a message", TODO()),
    MESSAGE_REACTION_REMOVE_All("all reactions were explicitly removed from a message", TODO()),
    PRESENCE_UPDATE("user was updated", TODO()),
    TYPING_START("user started typing in a channel", TODO()),
    USER_UPDATE("properties about the user changed", TODO()),
    VOICE_STATE_UPDATE("someone joined, left, or moved a voice channel", TODO()),
    VOICE_SERVER_UPDATE("guild's voice server was updated", TODO()),
    WEBHOOKS_UPDATE("guild channel webhook was created, update, or deleted", TODO());
}
