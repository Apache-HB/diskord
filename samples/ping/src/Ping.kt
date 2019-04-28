package samples

import com.serebit.strife.bot
import com.serebit.strife.commands.CommandsFeature
import com.serebit.strife.commands.command
import com.serebit.strife.entities.reply
import com.serebit.strife.onReady

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("No token passed.")

    bot(token) {
        logToConsole = true
        install(CommandsFeature())

        onReady { println("Connected to Discord!") }

        command("ping") {
            message.reply("Just Pong.")
        }

        command("ping") { param: Int ->
            message.reply("Pong. $param")
        }
    }
}
