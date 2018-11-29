package samples

import com.serebit.diskord.bot
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.logkat.LogLevel

fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    bot(token) {
        logLevel = LogLevel.TRACE

        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.content == "!ping") evt.message.reply("Pong.")
        }
    }
}
