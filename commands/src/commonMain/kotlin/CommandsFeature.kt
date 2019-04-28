package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotFeature
import com.serebit.strife.onMessage

class CommandsFeature(var prefix: String = "!") : BotFeature {
    override val name = "commands"
    private val parser = Parser()
    private val commands = mutableListOf<Command>()

    override fun installTo(scope: BotBuilder) {
        scope.onMessage {
            commands.forEach {
                parser.tokenize(message.content, it.prefixedSignature())?.let { tokens ->
                    parser.parseTokensOrNull(tokens, it.paramTypes)?.let { params ->
                        it.task.invoke(this@onMessage, params)
                    }
                }
            }
        }
    }

    internal fun addCommand(command: Command) {
        commands += command
    }

    private fun Command.prefixedSignature() = "^(\\Q$prefix\\E)$signature$".toRegex()
}
