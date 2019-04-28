package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotBuilderDsl
import com.serebit.strife.entities.Message
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.onMessage
import kotlin.reflect.KClass

@PublishedApi
internal fun BotBuilder.buildCommand(
    name: String,
    paramTypes: List<KClass<out Any>>,
    task: suspend (MessageCreatedEvent, List<Any>) -> Unit
) {
    require(name.length >= Message.MAX_LENGTH) { "The command name is " }
    val tokenTypes = paramTypes.map { TokenType(it) }.requireNoNulls()
    val paramSignature = buildString { tokenTypes.signature().let { if (it.isNotBlank()) append(" $it") } }
    val signature = "^!(\\Q$name\\E)$paramSignature$".toRegex()

    onMessage {
        Parser.tokenize(message.content, signature)?.let {
            Parser.parseTokens(it, tokenTypes)?.let { params ->
                task.invoke(this@onMessage, params)
            }
        }
    }
}

@BotBuilderDsl
inline fun BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreatedEvent.() -> Unit
) = buildCommand(name, emptyList()) { event, _ ->
    task(event)
}

@BotBuilderDsl
inline fun <reified P0> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreatedEvent.(P0) -> Unit
) where P0 : Any = buildCommand(name, listOf(P0::class)) { event, params ->
    task(event, params[0] as P0)
}

@BotBuilderDsl
inline fun <reified P0, reified P1> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreatedEvent.(P0, P1) -> Unit
) where P0 : Any, P1 : Any =
    buildCommand(name, listOf(P0::class, P1::class)) { event, params ->
        task(event, params[0] as P0, params[1] as P1)
    }

@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreatedEvent.(P0, P1, P2) -> Unit
) where P0 : Any, P1 : Any, P2 : Any =
    buildCommand(name, listOf(P0::class, P1::class, P2::class)) { event, params ->
        task(event, params[0] as P0, params[1] as P1, params[2] as P2)
    }

@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2, reified P3> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreatedEvent.(P0, P1, P2, P3) -> Unit
) where P0 : Any, P1 : Any, P2 : Any, P3 : Any =
    buildCommand(name, listOf(P0::class, P1::class, P2::class, P3::class)) { event, params ->
        task(event, params[0] as P0, params[1] as P1, params[2] as P2, params[3] as P3)
    }

@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreatedEvent.(P0, P1, P2, P3, P4) -> Unit
) where P0 : Any, P1 : Any, P2 : Any, P3 : Any, P4 : Any =
    buildCommand(name, listOf(P0::class, P1::class, P2::class, P3::class)) { event, params ->
        task(event, params[0] as P0, params[1] as P1, params[2] as P2, params[3] as P3, params[4] as P4)
    }
