package samples

import com.serebit.diskord.bot
import com.serebit.diskord.events.MessageCreatedEvent

fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    bot(token) {
        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.content == "!ping") evt.message.reply("Pong.")
        }
    }
}
