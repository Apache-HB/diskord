package samples

import com.serebit.strife.bot
import com.serebit.strife.commands.CommandsFeature
import com.serebit.strife.commands.command
import com.serebit.strife.entities.reply
import com.serebit.strife.onReady

suspend fun main(args: Array<String>) = bot(args[0]) {
    install(CommandsFeature())

    onReady { println("Connected to Discord!") }

    command("ping") {
        message.reply("Pong, but with commands!")
    }

    command("ping") { arg: Int ->
        message.reply("Pong, except you sent the number $arg as a parameter!")
    }
}
