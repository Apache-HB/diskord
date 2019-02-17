package samples

import com.serebit.strife.bot
import com.serebit.strife.onMessage
import com.serebit.strife.onReady

/**
 * An example of how to use Strife to connect to your bot account!
 *
 * TODO more docs
 */
suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    bot(token) {
        onReady {
            println("Connected to Discord!")
        }

        onMessage {
            if (message.content == "!ping") message.reply("Pong.")
        }
    }
}
