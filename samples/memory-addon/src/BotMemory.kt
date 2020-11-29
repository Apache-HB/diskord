package samples

import com.serebit.strife.*
import com.serebit.strife.botmemory.*
import com.serebit.strife.data.Color
import com.serebit.strife.entities.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * A simple example of a [Memory].
 *
 * @property id The ID of the entity this memory is of.
 * @property initTime When this memory was created.
 * @property prefix The prefix users can use to invoke commands.
 * @property activity UserIDs -> MessageCount. see [activityTracker] sample.
 */
data class MyBot(
    val id: Long,
    override val type: MemoryType,
    val initTime: Instant = Clock.System.now(),
    var prefix: String = "!",
    val activity: MutableMap<Long, Int> = mutableMapOf()
) : Memory {
    override fun toString(): String =
        "id: $id [type: ${type}, prefix: ${prefix}, activity_size: ${activity.size}]"
}

/** An example of how to use a [Memory] to save Server (Guild) specific information. */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrElse(0) { error("No token passed.") }

    // Start the bot building scope
    bot(token) {
        loggerLevel = LoggerLevel.INFO // Remove this to hide non-fatal log messages

        // Install the Memory Feature, having it remember Guilds as well as Users (in private channels)
        install(StrifeMemoryAddon<MyBot>()) {
            guild { MyBot(it.id, MemoryType.Guild) }
            user { MyBot(it.id, MemoryType.User) }
        }

        // This example will set the prefix the bot will respond to
        onMessageCreate {
            // Get the Memory associated with this guild
            // If the guild is null then nothing will happen
            memory<MyBot>(message.getGuild()?.id) {
                // Check for the current prefix with the command name and a parameter
                if (message.getContent().matches("(?i)${prefix}prefix\\s+.+".toRegex())) {
                    prefix = message.getContent().removePrefix(prefix + "prefix").trim() // Set the new prefix
                    message.reply("My prefix has been set to `$prefix`")
                }
            }
        }

        // You can also manually create memories for non Guild or User entities
        onMessageCreate {
            if (message.getContent() == "!remember") {
                remember(messageID) { MyBot(messageID, MemoryType.Message) }
                memory<MyBot>(messageID) { message.reply("I'll remember this message from `$initTime`") }
            }
        }

        // You can have all the stats about your memories sent in a nice Embed.
        onMessageCreate {
            if (message.getContent() == "!memory") memoryDebug(message.getChannel())
        }

        // Here's an example of a feature which tracks GuildMember's activity
        activityTracker()
    }

}

/** Track the number of messages each member sends */
val activityTracker: BotBuilder.() -> Unit = {
    onMessageCreate {
        // See how many messages each member has sent
        memory<MyBot>(message.getGuild()?.id) {
            when {
                message.getContent().matches(Regex("(?i)${this.prefix}count")) -> {
                    val sb = StringBuilder()
                    val top = activity.entries.sortedByDescending { it.value }
                        .run {
                            forEach { (id, count) ->
                                message.getGuild()!!.getMember(id)?.run {
                                    sb.append(getUser().getDisplayName()).append(": $count")
                                }
                            }
                            firstOrNull()?.key?.let { message.getGuild()!!.getMember(it) }
                        }
                    message.reply {
                        title("Member Activity (by messages sent)")
                        description = sb.takeIf { it.isNotEmpty() }?.toString() ?: "No activity yet."
                        color = Color(activity.values.sum() * 100)
                        image = top?.getUser()?.getAvatar()?.uri
                        author {
                            name = context.selfUser.getUsername()
                            imgUrl = context.selfUser.getAvatar().uri
                        }
                        footer {
                            text = "Run with Strife"
                            imgUrl = StrifeInfo.logoUri
                            timestamp = Clock.System.now()
                        }
                    }
                }
                message.getAuthor()?.isHuman() == true -> // Track the number of messages each member sends
                    activity[message.getAuthor()!!.id] = activity[message.getAuthor()!!.id]?.plus(1) ?: 1
            }
        }
    }
}
