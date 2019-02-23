package samples

import com.serebit.strife.bot

/** An example of how to use Strife to connect to your bot account and respond to a message! */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrNull(0) ?: error("No token passed.")

    // Start the bot building scope
    bot(token) {
        // Print to console when the bot is connected & ready
        onReady { println("Connected to Discord!") }

        // On "!ping" messages, send PONG!
        onMessage {
            if (message.content == "!ping") {
                message.reply("Pong!")?.also {
                    it.edit("Pong! ${(it.createdAt - message.createdAt).millisecondsLong}ms")
                }
            }
        }
    }
}
