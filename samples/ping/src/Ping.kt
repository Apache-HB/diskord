package samples

import com.serebit.strife.*
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.entities.*
import com.serebit.strife.events.GuildRoleCreateEvent
import com.serebit.strife.events.GuildRoleUpdateEvent

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

        var Nrole: GuildRole? = null

        onMessageCreate {
            when {
                message.content == "!role" -> {
                    message.guild?.createRole("NEW ROLE", listOf(Permission.AddReactions), Color.PINK, false, false)
                }
                message.content == "!edit" -> {
                    Nrole?.setColor(Color.CYAN)
                    Nrole?.addPermissions(Permission.ChangeNickname, Permission.BanMembers)
                    Nrole?.removePermissions(Permission.BanMembers)
                    Nrole?.hoist()
                    Nrole?.setMentionable(true)
                    Nrole?.setName("NEW ROLE EDITED")
                }
                message.content == "!up" -> Nrole?.raise()
                message.content == "!down" -> Nrole?.lower()
            }
        }

        onEvent<GuildRoleCreateEvent> {
            Nrole = role
            guild.getTextChannel(439522471369244683)!!.send {
                color = role.color
                titleText = role.name
                description = role.permissions.joinToString { "\n${it.name}" } + "\n" + role.isHoisted
            }
        }

        onEvent<GuildRoleUpdateEvent> {
            guild.getTextChannel(439522471369244683)!!.send {
                color = role.color
                titleText = role.name
                description = role.permissions.joinToString { "\n${it.name}" } + "\n" + role.isHoisted
            }
        }
    }
}
