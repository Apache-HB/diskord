package samples

import com.serebit.strife.Context
import com.serebit.strife.bot
import com.serebit.strife.data.Color
import com.serebit.strife.entities.Embed
import com.serebit.strife.entities.Embed.Field
import com.serebit.strife.onMessage
import com.serebit.strife.onReady

/** An example of how to use Strife to send a [Message Embed][Embed]. */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrNull(0) ?: error("No token passed.")

    // Start the bot building scope
    bot(token) {
        logToConsole = true // Uncomment this to see log messages
        // Print to console when the bot is connected & ready
        onReady { println("Connected to Discord!") }

        // On "!embed", send the embed
        onMessage {
            if (message.content == "!embed") {
                message.reply {
                    text = "This Embed was sent using Strife!"
                    embed {
                        author {
                            name = context.selfUser.username
                            imgUrl = context.selfUser.avatar.uri
                            url = Context.sourceUri
                        }

                        // title("can also be done", "https://like_this.gov")
                        title {
                            title = "An Embed made with Strife!"
                            url = Context.sourceUri
                        }

                        description = """
                            This is the description of the embed. It appears right after the
                            title and supports [links](https://google.com) and *basic* **Discord**
                            __markdown__ ``formatting``
                        """.trimIndent()

                        color = Color.GREEN

                        // Fields can be added like this
                        field {
                            name = "This is a Field Name (i.e. Title)"
                            content = "This is the Field Content (aka value)"
                        }
                        // Or like this
                        field("This is also a Field") {
                            "This is the field content, made within a lambda!"
                        }
                        // Inline Fields can be made like this
                        inlineField {
                            name = "This field is Inlined"
                            content = "It will appear next to other inline fields."
                        }
                        // Or the old way
                        field {
                            name = "This is also Inlined"
                            content = "And it's kinda cool."
                            inline = true
                        }
                        // Fields can also be manually added (but this is less cool)
                        fields.add(Field("This Field was made and added manually", "And it's lame", false))

                        // Set the thumbnail (the smaller image in the upper right of the embed)
                        thumbnail(context.selfUser.avatar.uri)

                        // Set the large image at the bottom of the embed
                        image(context.selfUser.avatar.uri)

                        // Set the footer at the bottom of the embed
                        footer {
                            text = "This post was made by Strife Gang"
                            imgUrl = context.selfUser.avatar.uri
                            timestamp = message.createdAt
                        }
                    }
                }
            }
        }
    }
}
