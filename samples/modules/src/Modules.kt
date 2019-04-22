package samples

import com.serebit.strife.*
import com.serebit.strife.entities.reply

suspend fun main(args: Array<String>) = bot(args[0]) {
    install { ExampleModule() }
}

class ExampleModule : BotModule({
    onReady { println("Connected to Discord!") }

    onMessage {
        if (message.content == "!ping")
            message.reply("Pong, but with modules!")
    }
})
