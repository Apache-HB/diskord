package samples

import com.serebit.strife.bot
import com.serebit.strife.entities.UnicodeEmoji
import com.serebit.strife.entities.reply
import com.serebit.strife.onGuildRoleUpdate
import com.serebit.strife.onMessageCreate
import com.serebit.strife.onReady

/**
 * An example of how to use Strife to connect
 * to your bot account and respond to a message!
 */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrNull(0) ?: error("No token passed.")

    // Start the bot building scope
    bot(token) {
        logToConsole = true // Remove or set this to false to hide log messages

        // Print to console when the bot is connected & ready
        onReady { println("Connected to Discord!") }

        // On "!ping" messages, send PONG!
        onMessageCreate {
            if (message.content == "!ping") message.reply("Pong! ${UnicodeEmoji.PingPong}")
        }


        ////////////////////////////////////////////////////////////////
        // Route MR Testing
        // DELETE BEFORE MERGE

        val roleID = 500369621564784642L
        onMessageCreate {
            val meID = 451005806222245889L
            if (message.content != "!test") return@onMessageCreate
            val guild = message.guild!!
            val me = guild.getMember(meID)!!
            val mrole = guild.getRole(roleID)!!
            ////////////////////////////////////////////
            println(mrole.position)
            mrole.setPosition(32)
        }

        onGuildRoleUpdate {
            if (roleID == role.id) println(role.position)
        }

    }
}
