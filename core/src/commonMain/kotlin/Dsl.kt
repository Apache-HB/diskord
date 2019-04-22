package com.serebit.strife

import com.serebit.strife.events.Event
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.events.ReadyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * DSL marker for extension functions on the class [BotBuilder].
 */
@DslMarker
annotation class BotBuilderDsl

/**
 * Creates a new [CoroutineScope] in which to launch bots using [launchBot]. This method will only finish once all
 * bots launched within it using [launchBot] have completed.
 */
@BotBuilderDsl
suspend inline fun botScope(noinline block: suspend CoroutineScope.() -> Unit) = coroutineScope(block)

/**
 * Creates a new instance of the [Context] base class. This is the recommended method of initializing a Discord bot
 * using this library. Event listeners should be declared in the [init] block.
 *
 * @param token The Discord-provided token used to connect to Discord's servers. A token can be obtained from
 * https://discordapp.com/developers/applications/me.
 */
@BotBuilderDsl
suspend inline fun bot(token: String, init: BotBuilder.() -> Unit = {}) {
    BotBuilder(token).apply(init).build()?.connect()
}

/**
 * Creates a new instance of the [Context] base class, and launches it inside a coroutine. This is ideal for
 * projects that run multiple bots, as it does not block the scope it is run within. Should be used from inside
 * [botScope], or within any of the [CoroutineScope] builders in kotlinx.coroutines. Event listeners should be
 * declared in the [init] block.
 *
 * @param token The Discord-provided token used to connect to Discord's servers. A token can be obtained from
 * https://discordapp.com/developers/applications/me.
 */
@BotBuilderDsl
inline fun CoroutineScope.launchBot(token: String, crossinline init: BotBuilder.() -> Unit = {}) {
    launch { bot(token, init) }
}

/**
 * Creates an event listener for events with type T. The code inside the [task] block will be executed every time
 * the bot receives an event with type T.
 */
@BotBuilderDsl
inline fun <reified T : Event> BotBuilder.onEvent(noinline task: suspend T.() -> Unit) = onEvent(T::class, task)

/** Convenience method to create an event listener that will execute on reception of a ReadyEvent. */
@BotBuilderDsl
fun BotBuilder.onReady(task: suspend ReadyEvent.() -> Unit) = onEvent(ReadyEvent::class, task)

/** Convenience method to create an event listener that will execute when a message is created. */
@BotBuilderDsl
fun BotBuilder.onMessage(task: suspend MessageCreatedEvent.() -> Unit) = onEvent(MessageCreatedEvent::class, task)

/** Install (add) a [BotModule] to the [BotBuilder]. */
@BotBuilderDsl
suspend fun BotBuilder.install(module: () -> BotModule) { module().also { it.init(this) } }
