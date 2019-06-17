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

/**
 * Creates an event listener for events with type T. The code inside the [task] block will be executed every time
 * the bot receives an event with type T.
 */
@BotBuilderDsl
inline fun <reified T : Event> BotBuilder.onEvent(noinline task: suspend T.() -> Unit) = onEvent(T::class, task)

/** Convenience method to create an event listener that will execute on reception of a ReadyEvent. */
@BotBuilderDsl
fun BotBuilder.onReady(task: suspend ReadyEvent.() -> Unit): Unit = onEvent(ReadyEvent::class, task)

/** Convenience method to create an event listener that will execute when a message is created. */
@BotBuilderDsl
fun BotBuilder.onMessage(task: suspend MessageCreatedEvent.() -> Unit): Unit = onEvent(MessageCreatedEvent::class, task)
