package samples

import com.serebit.strife.*
import com.serebit.strife.entities.UnicodeEmoji
import com.serebit.strife.entities.reply
import com.serebit.strife.events.MessageCreateEvent
import com.serebit.strife.internal.EventResult

/**
 * An example of how to use Strife to connect
 * to your bot account and respond to a message!
 */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrNull(0) ?: error("No token passed.")

    // Start the bot building scope
    bot(token) {
        logToConsole = true // Remove or set this to false to hide log messages

        // Print to console when the bot is connected & ready
        onReady { println("Connected to Discord!") }

        // On "!ping" messages, send PONG!
        onMessageCreate {
            if (message.content == "!ping") message.reply("Pong! ${UnicodeEmoji.PingPong}")
        }
    }
}
