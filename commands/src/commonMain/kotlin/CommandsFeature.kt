package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotFeature
import com.serebit.strife.onMessage

class CommandsFeature(var prefix: String = "!") : BotFeature {
    override val name = "Commands"
    private val parser = Parser()
    private val commands = mutableListOf<Command>()

    override fun installTo(scope: BotBuilder) = scope.onMessage {
        commands.forEach { command ->
            parser.parse(message.content, command.prefixedSignature, command.paramTypes)?.also { params ->
                command.invoke(this, params)
            }
        }
    }

    internal fun addCommand(command: Command) {
        commands += command
    }

    private val Command.prefixedSignature get() = "^(\\Q$prefix\\E)$signature$".toRegex()
}
