package samples

import com.serebit.strife.bot
import com.serebit.strife.events.MessageCreatedEvent

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    bot(token) {
        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.content == "!ping") evt.message.reply("Pong.")
        }
    }
}
