package com.serebit.strife

import com.serebit.strife.data.Presence
import com.serebit.strife.events.*
import com.serebit.strife.internal.EventResult
import com.serebit.strife.internal.dispatches.GuildBanAdd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

/**
 * DSL marker for extension functions on the class [BotBuilder].
 */
@DslMarker
annotation class BotBuilderDsl

/**
 * Creates a new coroutine scope in which to launch bots using [launchBot]. This method will only finish once all
 * bots launched within it using [launchBot] have completed.
 */
@BotBuilderDsl
suspend inline fun botScope(noinline block: suspend CoroutineScope.() -> Unit): Unit = coroutineScope(block)

/**
 * Creates a new instance of the [BotClient] base class. This is the recommended method of initializing a bot using
 * this library. The [token] is provided by Discord [at their website](https://discordapp.com/developers/applications).
 * Event listeners should be declared in the [init] block.
 */
@BotBuilderDsl
suspend inline fun bot(token: String, init: BotBuilder.() -> Unit = {}) {
    BotBuilder(token).apply(init).build()?.connect()
}

/**
 * Creates a new instance of the [BotClient] base class, and launches it inside a coroutine. This is ideal for
 * projects that run multiple bots, as it does not block the scope it is run within. Should be used from inside
 * [botScope], or within any of the coroutine scope builders in kotlinx.coroutines.
 * The [token] is provided by Discord [at their website](https://discordapp.com/developers/applications). Event
 * listeners should be declared in the [init] block.
 */
@BotBuilderDsl
inline fun CoroutineScope.launchBot(token: String, crossinline init: BotBuilder.() -> Unit = {}) {
    launch { bot(token, init) }
}


/*
 * Listeners
 */


// ==> Generic Events //

/**
 * Creates a terminable event listener of type [T], which will run the given [task] [successfulRunLimit] number of
 * [successful][EventResult.SUCCESS] times before being terminated.
 */
@BotBuilderDsl
@UseExperimental(ExperimentalStdlibApi::class)
inline fun <reified T : Event> BotBuilder.onEventLimited(
    successfulRunLimit: Int = 1,
    noinline task: suspend T.() -> EventResult
) = onTerminableEvent(typeOf<T>(), successfulRunLimit, task)

/**
 * Creates an event listener for events with type [T]. The code inside the [task] block will be executed every time the
 * bot receives an event with type [T].
 */
@BotBuilderDsl
@UseExperimental(ExperimentalStdlibApi::class)
inline fun <reified T : Event> BotBuilder.onEvent(noinline task: suspend T.() -> Unit) = onEvent(typeOf<T>(), task)

// ==> Status Events //

/** Convenience method to create an event listener that will execute when the bot starts a session. */
@BotBuilderDsl
fun BotBuilder.onReady(task: suspend ReadyEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when the bot resumes a session. */
@BotBuilderDsl
fun BotBuilder.onResume(task: suspend ResumeEvent.() -> Unit) = onEvent(task)

// ==> Message & Reaction Events //

/** Convenience method to create an event listener that will execute on any [MessageEvent]. */
@BotBuilderDsl
fun BotBuilder.onMessageEvent(task: suspend MessageEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a message is created. */
@BotBuilderDsl
fun BotBuilder.onMessageCreate(task: suspend MessageCreateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a message's content is edited. */
@BotBuilderDsl
fun BotBuilder.onMessageEdit(task: suspend MessageEditEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a message is deleted. */
@BotBuilderDsl
fun BotBuilder.onMessageDelete(task: suspend MessageDeleteEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a message reaction is added. */
@BotBuilderDsl
fun BotBuilder.onReactionAdd(task: suspend MessageReactionAddEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a message reaction is removed. */
@BotBuilderDsl
fun BotBuilder.onReactionRemove(task: suspend MessageReactionRemoveEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when all reactions are cleared from a message . */
@BotBuilderDsl
fun BotBuilder.onReactionClear(task: suspend MessageReactionRemoveAllEvent.() -> Unit) = onEvent(task)

// ==> Channel Events //

/** Convenience method to create an event listener that will execute on any [ChannelEvent] . */
@BotBuilderDsl
fun BotBuilder.onChannelEvent(task: suspend ChannelEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a channel is created. */
@BotBuilderDsl
fun BotBuilder.onChannelCreate(task: suspend ChannelCreateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a channel is updated. */
@BotBuilderDsl
fun BotBuilder.onChannelUpdate(task: suspend ChannelUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a channel is deleted. */
@BotBuilderDsl
fun BotBuilder.onChannelDelete(task: suspend ChannelDeleteEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a channel's pinned messages are updated. */
@BotBuilderDsl
fun BotBuilder.onChannelPinsUpdate(task: suspend ChannelPinsUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a user begins typing in a channel. */
@BotBuilderDsl
fun BotBuilder.onTypingStart(task: suspend TypingStartEvent.() -> Unit) = onEvent(task)

// ==> Guild events //

/** Convenience method to create an event listener that will execute on any [GuildEvent]. */
@BotBuilderDsl
fun BotBuilder.onGuildEvent(task: suspend GuildEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild is created or joined. */
@BotBuilderDsl
fun BotBuilder.onGuildCreate(task: suspend GuildCreateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild is updated. */
@BotBuilderDsl
fun BotBuilder.onGuildUpdate(task: suspend GuildUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild becomes unavailable. */
@BotBuilderDsl
fun BotBuilder.onGuildDelete(task: suspend GuildDeleteEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Ban is added or removed. */
@BotBuilderDsl
fun BotBuilder.onGuildBanEvent(task: suspend GuildBanEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Ban is added. */
@BotBuilderDsl
fun BotBuilder.onGuildBanAdd(task: suspend GuildBanAddEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Ban is removed. */
@BotBuilderDsl
fun BotBuilder.onGuildBanRemove(task: suspend GuildBanRemoveEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when on any [GuildMemberEvent]. */
@BotBuilderDsl
fun BotBuilder.onGuildMemberEvent(task: suspend GuildMemberEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a User joins a Guild. */
@BotBuilderDsl
fun BotBuilder.onGuildMemberJoin(task: suspend GuildMemberJoinEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Member is updated. */
@BotBuilderDsl
fun BotBuilder.onGuildMemberUpdate(task: suspend GuildMemberUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a User leaves a Guild . */
@BotBuilderDsl
fun BotBuilder.onGuildMemberLeave(task: suspend GuildMemberLeaveEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute on any [GuildRoleEvent]. */
@BotBuilderDsl
fun BotBuilder.onGuildRoleEvent(task: suspend GuildRoleEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Role is created. */
@BotBuilderDsl
fun BotBuilder.onGuildRoleCreate(task: suspend GuildRoleCreateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Role is updated. */
@BotBuilderDsl
fun BotBuilder.onGuildRoleUpdate(task: suspend GuildRoleUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild Role is deleted. */
@BotBuilderDsl
fun BotBuilder.onGuildRoleDelete(task: suspend GuildRoleDeleteEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild's Emoji are updated. */
@BotBuilderDsl
fun BotBuilder.onGuildEmojiUpdate(task: suspend GuildEmojisUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a Guild's integrations have been update. */
@BotBuilderDsl
fun BotBuilder.onGuildIntegrationsUpdate(task: suspend GuildIntegrationsUpdateEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a User's [Presence] is updated. */
@BotBuilderDsl
fun BotBuilder.onPresenceUpdate(task: suspend PresenceUpdateEvent.() -> Unit) = onEvent(task)
