package com.serebit.strife.scripting

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.strife.BotBuilder
import com.serebit.strife.BotFeature
import com.serebit.strife.BotFeatureProvider
import kotlinx.coroutines.InternalCoroutinesApi

/**
 * This [BotFeature] allows configuration of a [BotBuilder] via Kotlin scripts for better organization of individual bot
 * functions. The feature takes a path, finds the file at that path, and attempts to compile it and apply it against the
 * builder.
 */
class ScriptingFeature : BotFeature {
    override val name: String = "Scripting"
    private lateinit var compiler: Compiler

    @OptIn(InternalCoroutinesApi::class)
    override fun installTo(scope: BotBuilder) {
        val logger = Logger(if (scope.logToConsole) LogLevel.TRACE else LogLevel.OFF)
        compiler = Compiler(scope, logger)
    }

    internal fun compileScript(text: String) = compiler.eval(text)

    /** The provider for this feature. Should be used with [BotBuilder.install]. */
    companion object Provider : BotFeatureProvider<ScriptingFeature> {
        override fun provide(): ScriptingFeature = ScriptingFeature()
    }
}
