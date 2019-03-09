package com.serebit.strife

import com.serebit.strife.events.Event
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.events.ReadyEvent

@DslMarker
annotation class StrifeDsl

/**
 * Creates a new instance of the [Context] base class. This is the recommended method of initializing a Discord bot
 * using this library.
 *
 * @param token The Discord-provided token used to connect to Discord's servers. A token can be obtained from
 * https://discordapp.com/developers/applications/me.
 *
 * @param init The initialization block. Event listeners should be declared here using the provided methods in
 * [BotBuilder].
 */
@StrifeDsl
suspend inline fun bot(token: String, init: BotBuilder.() -> Unit = {}) {
    BotBuilder(token).apply(init).build()?.connect()
}

/**
 * Creates an event listener for events with type T. The code inside the [task] block will be executed every time
 * the bot receives an event with type T.
 */
@StrifeDsl
inline fun <reified T : Event> BotBuilder.onEvent(noinline task: suspend T.() -> Unit) = onEvent(T::class, task)

/** Convenience method to create an event listener that will execute on reception of a ReadyEvent. */
@StrifeDsl
fun BotBuilder.onReady(task: suspend ReadyEvent.() -> Unit) = onEvent(task)

/** Convenience method to create an event listener that will execute when a message is created. */
@StrifeDsl
fun BotBuilder.onMessage(task: suspend MessageCreatedEvent.() -> Unit) = onEvent(task)
