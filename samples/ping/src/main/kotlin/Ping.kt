package samples

import com.serebit.diskord.diskord
import com.serebit.diskord.events.MessageCreatedEvent

fun main(args: Array<String>) {
    diskord("Token goes here") {
        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.content == "!ping") evt.message.reply("Pong.")
        }
    }
}
