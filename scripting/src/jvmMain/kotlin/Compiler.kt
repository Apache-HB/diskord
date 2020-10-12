package com.serebit.strife.scripting

import com.serebit.logkat.Logger
import com.serebit.logkat.error
import com.serebit.strife.BotBuilder
import kotlin.reflect.typeOf
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

@KotlinScript(displayName = "Strife Configuration", fileExtension = Compiler.SCRIPT_EXTENSION)
internal abstract class StrifeConfigScript

internal class Compiler(receiver: BotBuilder, private val logger: Logger) {
    private val evaluationConfiguration = ScriptEvaluationConfiguration {
        implicitReceivers(receiver)
    }
    private val host = BasicJvmScriptingHost()

    @OptIn(ExperimentalStdlibApi::class)
    private val config = createJvmCompilationConfigurationFromTemplate<StrifeConfigScript> {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
            implicitReceivers(typeOf<BotBuilder>())
        }
    }

    fun eval(scriptText: String) {
        ScriptEvaluationConfiguration
        host.eval(scriptText.toScriptSource(), config, evaluationConfiguration)
            .reports
            .filter { it.severity == ScriptDiagnostic.Severity.ERROR }
            .forEach { logger.error("Error in script evaluation: ${it.message}") }
    }

    companion object {
        const val SCRIPT_EXTENSION = "strife.kts"
    }
}
