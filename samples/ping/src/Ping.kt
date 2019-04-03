package samples

import com.serebit.strife.bot
import com.serebit.strife.entities.reply
import com.serebit.strife.onMessage
import com.serebit.strife.onReady

suspend fun main(args: Array<String>) {
    val token = args.getOrElse(0) { error("No token passed.") }

    bot(token) {
        logToConsole = true

        onReady { println("Connected to Discord!") }

        onMessage {
            if (message.content == "!ping") message.reply("Pong.")
        }
    }
}
