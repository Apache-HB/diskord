package com.serebit.strife.scripting

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.strife.BotAddon
import com.serebit.strife.BotAddonProvider
import com.serebit.strife.BotBuilder
import kotlinx.coroutines.InternalCoroutinesApi

/**
 * This [BotAddon] allows configuration of a [BotBuilder] via Kotlin scripts for better organization of individual bot
 * functions. The addon takes a path, finds the file at that path, and attempts to compile it and apply it against the
 * builder.
 */
class ScriptingAddon : BotAddon {
    override val name: String = "Scripting"
    private lateinit var compiler: Compiler

    @OptIn(InternalCoroutinesApi::class)
    override fun installTo(scope: BotBuilder) {
        val logger = Logger(if (scope.logToConsole) LogLevel.TRACE else LogLevel.OFF)
        compiler = Compiler(scope, logger)
    }

    internal fun compileScript(text: String) = compiler.eval(text)

    /** The provider for this addon. Should be used with [BotBuilder.install]. */
    companion object Provider : BotAddonProvider<ScriptingAddon> {
        override fun provide(): ScriptingAddon = ScriptingAddon()
    }
}
