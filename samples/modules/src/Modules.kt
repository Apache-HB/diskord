package samples

import com.serebit.strife.*
import com.serebit.strife.entities.reply

/**
 * An example showing the use of a [BotModule].
 *
 * Pass the bot token through the program arguments.
 */
suspend fun main(args: Array<String>) = bot(args[0]) {
    // Add the module to the bot
    install { ExampleModule() }
}

/**
 * An example of a [BotModule]. The module is used just like the [bot]/[BotBuilder] DSL
 * and is useful for cleaning up or reusing code!
 */
class ExampleModule : BotModule({
    onReady { println("Connected to Discord!") }

    onMessage {
        if (message.content == "!ping")
            message.reply("Pong, but with modules!")
    }
})
