package samples

import com.serebit.diskord.diskord
import com.serebit.diskord.events.MessageCreatedEvent

fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    diskord(token) {
        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.content == "!ping") evt.message.reply("Pong.")
        }
    }
}
