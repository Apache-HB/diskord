package samples

import com.serebit.strife.LoggerLevel
import com.serebit.strife.bot
import com.serebit.strife.scripting.ScriptingAddon
import com.serebit.strife.scripting.applyScriptFromResource

/**
 * An example of how to use Strife to connect
 * to your bot account and respond to a message!
 */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrNull(0) ?: error("No token passed.")

    // Start the bot building scope
    bot(token) {
        loggerLevel = LoggerLevel.INFO // Remove this to hide non-fatal log messages

        install(ScriptingAddon)

        // all the configuration is done from the script in the jarfile!
        applyScriptFromResource("test.strife.kts")
    }
}
