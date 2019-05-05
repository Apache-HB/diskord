package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotBuilderDsl
import com.serebit.strife.entities.Message
import com.serebit.strife.events.MessageCreatedEvent
import kotlin.reflect.KClass

private val BotBuilder.commandsFeature get() = (features.getValue("Commands") as CommandsFeature)

@PublishedApi
internal fun BotBuilder.buildCommand(
    name: String,
    paramClasses: List<KClass<out Any>>,
    task: suspend (MessageCreatedEvent, List<Any>) -> Unit
) {
    require("Commands" in features) {
        "The Commands feature must be installed before any commands can be added."
    }
    require(name.length < Message.MAX_LENGTH) { "The command name is too long." }

    val paramTypes = paramClasses.map { ParamType(it) }.requireNoNulls()

    commandsFeature.addCommand(Command(name, paramTypes, task))
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
