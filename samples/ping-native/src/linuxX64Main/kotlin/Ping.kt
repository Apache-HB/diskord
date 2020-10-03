package samples

import com.serebit.strife.bot
import com.serebit.strife.entities.reply
import com.serebit.strife.onMessageCreate
import com.serebit.strife.onReady
import kotlinx.coroutines.runBlocking

/**
 * An example of how to use Strife to connect
 * to your bot account and respond to a message!
 */
fun main(args: Array<String>) = runBlocking {
    // Pass the bot secret token as an arg
    val token = args.getOrNull(0) ?: error("No token passed.")

    // Start the bot building scope
    bot(token) {
        logToConsole = true // Remove or set this to false to hide log messages

        // Print to console when the bot is connected & ready
        onReady { println("Connected to Discord!") }

        // On "!ping" messages, send PONG!
        onMessageCreate {
            if (message.getContent() == "!ping")
                message.reply("Pong! Latency is ${context.gatewayLatency}ms.")
        }
    }
}
