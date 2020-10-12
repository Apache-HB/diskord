package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotBuilderDsl
import com.serebit.strife.entities.Message
import com.serebit.strife.events.MessageCreateEvent
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private val BotBuilder.commandsAddon get() = (addons.getValue("Commands") as CommandsAddon)

@PublishedApi
internal fun BotBuilder.buildCommand(
    name: String,
    paramClasses: List<KType>,
    task: suspend (MessageCreateEvent, List<Any>) -> Unit
) {
    require("Commands" in addons) {
        "The Commands addon must be installed before any commands can be added."
    }
    require(name.length < Message.MAX_LENGTH) { "The command name is too long." }

    val paramTypes = paramClasses.map { ParamType(it) }.requireNoNulls()

    commandsAddon.addCommand(Command(name, paramTypes, task))
}

/**
 * Creates a command with the given [name], and a [task] with no parameters. Will throw an exception if the name is too
 * long (must be less than [Message.MAX_LENGTH] characters).
 */
@BotBuilderDsl
inline fun BotBuilder.command(name: String, crossinline task: suspend MessageCreateEvent.() -> Unit) {
    buildCommand(name, emptyList()) { event, _ ->
        task(event)
    }
}

/**
 * Creates a command with the given [name], and a [task] with a single parameter of type [P0]. Will throw an exception
 * if the parameter type is invalid, or if the [name] is too long (must be less than [Message.MAX_LENGTH] characters).
 */
@OptIn(ExperimentalStdlibApi::class)
@BotBuilderDsl
inline fun <reified P0> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreateEvent.(P0) -> Unit
) where P0 : Any {
    buildCommand(name, listOf(typeOf<P0>())) { event, params ->
        task(event, params[0] as P0)
    }
}

/**
 * Creates a command with the given [name], and a [task] with two parameters of types [P0] and [P1]. Will throw an
 * exception if any parameter types are invalid, or if the [name] is too long (must be less than [Message.MAX_LENGTH]
 * characters).
 */
@OptIn(ExperimentalStdlibApi::class)
@BotBuilderDsl
inline fun <reified P0, reified P1> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreateEvent.(P0, P1) -> Unit
) where P0 : Any, P1 : Any {
    buildCommand(name, listOf(typeOf<P0>(), typeOf<P1>())) { event, params ->
        task(event, params[0] as P0, params[1] as P1)
    }
}

/**
 * Creates a command with the given [name], and a [task] with three parameters of types [P0], [P1], and [P2]. Will throw
 * an exception if any parameter types are invalid, or if the [name] is too long (must be less than [Message.MAX_LENGTH]
 * characters).
 */
@OptIn(ExperimentalStdlibApi::class)
@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreateEvent.(P0, P1, P2) -> Unit
) where P0 : Any, P1 : Any, P2 : Any {
    buildCommand(name, listOf(typeOf<P0>(), typeOf<P1>(), typeOf<P2>())) { event, params ->
        task(event, params[0] as P0, params[1] as P1, params[2] as P2)
    }
}

/**
 * Creates a command with the given [name], and a [task] with four parameters of types [P0], [P1], [P2] and [P3]. Will
 * throw an exception if any parameter types are invalid, or if the [name] is too long (must be less than
 * [Message.MAX_LENGTH] characters).
 */
@OptIn(ExperimentalStdlibApi::class)
@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2, reified P3> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreateEvent.(P0, P1, P2, P3) -> Unit
) where P0 : Any, P1 : Any, P2 : Any, P3 : Any {
    buildCommand(name, listOf(typeOf<P0>(), typeOf<P1>(), typeOf<P2>(), typeOf<P3>())) { event, params ->
        task(event, params[0] as P0, params[1] as P1, params[2] as P2, params[3] as P3)
    }
}

/**
 * Creates a command with the given [name], and a [task] with five parameters of types [P0], [P1], [P2], [P3], and [P4].
 * Will throw an exception if any parameter types are invalid, or if the [name] is too long (must be less than
 * [Message.MAX_LENGTH] characters).
 */
@OptIn(ExperimentalStdlibApi::class)
@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreateEvent.(P0, P1, P2, P3, P4) -> Unit
) where P0 : Any, P1 : Any, P2 : Any, P3 : Any, P4 : Any {
    buildCommand(name, listOf(typeOf<P0>(), typeOf<P1>(), typeOf<P2>(), typeOf<P3>(), typeOf<P4>())) { event, params ->
        task(event, params[0] as P0, params[1] as P1, params[2] as P2, params[3] as P3, params[4] as P4)
    }
}

/**
 * Creates a command with the given [name], and a [task] with six parameters of types [P0], [P1], [P2], [P3], [P4], and
 * [P5]. Will throw an exception if any parameter types are invalid, or if the [name] is too long (must be less than
 * [Message.MAX_LENGTH] characters).
 */
@OptIn(ExperimentalStdlibApi::class)
@BotBuilderDsl
inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, reified P5> BotBuilder.command(
    name: String,
    crossinline task: suspend MessageCreateEvent.(P0, P1, P2, P3, P4, P5) -> Unit
) where P0 : Any, P1 : Any, P2 : Any, P3 : Any, P4 : Any, P5 : Any {
    buildCommand(
        name,
        listOf(typeOf<P0>(), typeOf<P1>(), typeOf<P2>(), typeOf<P3>(), typeOf<P4>(), typeOf<P5>())
    ) { event, params ->
        task(
            event, params[0] as P0, params[1] as P1, params[2] as P2, params[3] as P3, params[4] as P4, params[5] as P5
        )
    }
}
