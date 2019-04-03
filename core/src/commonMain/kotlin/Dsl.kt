package com.serebit.strife

import com.serebit.strife.events.Event
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.events.ReadyEvent

/**
 * DSL marker for extension functions on the class [BotBuilder].
 */
@DslMarker
annotation class BotBuilderDsl

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
@BotBuilderDsl
suspend inline fun bot(token: String, init: BotBuilder.() -> Unit = {}) {
    BotBuilder(token).apply(init).build()?.connect()
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
