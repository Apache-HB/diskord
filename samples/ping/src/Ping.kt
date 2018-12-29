package samples

import com.serebit.strife.bot
import com.serebit.strife.onMessage

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    bot(token) {
        onMessage {
            if (message.content == "!ping") message.reply("Pong.")
        }
    }
}
