package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotFeature
import com.serebit.strife.BotFeatureProvider
import com.serebit.strife.onMessageCreate
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

/**
 * The [BotFeature] that controls building and running commands for the [com.serebit.strife.BotClient] class. This
 * feature must be installed to a client before commands can be added to it.
 *
 * @property prefix The command prefix for the client to use.
 */
class CommandsFeature(var prefix: String = "!") : BotFeature {
    override val name: String = "Commands"
    private val parser = Parser()
    private val commands = mutableListOf<Command>()

    @OptIn(InternalCoroutinesApi::class)
    override fun installTo(scope: BotBuilder) {
        scope.onMessageCreate {
            commands.asFlow().collect { command ->
                parser.parse(message.getContent(), command.prefixedSignature, command.paramTypes)?.also { params ->
                    command.invoke(this, params)
                }
            }
        }
    }

    internal fun addCommand(command: Command) {
        commands += command
    }

    private val Command.prefixedSignature get() = "^(\\Q$prefix\\E)$signature$".toRegex()

    /** The provider for this feature. Should be used with [BotBuilder.install]. */
    companion object Provider : BotFeatureProvider<CommandsFeature> {
        override fun provide(): CommandsFeature = CommandsFeature()
    }
}
