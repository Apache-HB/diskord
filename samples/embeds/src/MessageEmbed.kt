package samples

import com.serebit.strife.StrifeInfo
import com.serebit.strife.bot
import com.serebit.strife.data.Color
import com.serebit.strife.entities.*
import com.serebit.strife.onMessageCreate
import com.serebit.strife.onReady
import com.serebit.strife.text.*

/** An example of how to use Strife to send a [Message Embed][Embed]. */
suspend fun main(args: Array<String>) {
    // Pass the bot secret token as an arg
    val token = args.getOrElse(0) { error("No token passed.") }

    // Start the bot building scope
    bot(token) {
        // logToConsole = true // Uncomment this to see log messages

        // Print to console when the bot is connected & ready
        onReady { println("Connected to Discord!") }

        var embedMessage: Message? = null
        // On "!embed", send the embed
        onMessageCreate {
            if (message.content() == "!embed") {
                embedMessage = message.reply("This embed was sent using Strife!") {
                    author {
                        name = context.selfUser.getUsername()
                        imgUrl = context.selfUser.getAvatar().uri
                        url = StrifeInfo.sourceUri
                    }

                    title("An embed made with Strife!", StrifeInfo.sourceUri)

                    description = """
                            This is the description of the embed. It appears right after the |
                            title and supports ${"links".inlineLink(StrifeInfo.sourceUri)} and ${"basic".italic}
                            ${"Discord".bold} ${"markdown".underline} ${"formatting".inlineCode}. Even
                            ${"stylized".strikethrough} ${"fun codeblocks() {}".codeBlock("kotlin")}
                        """.trimIndent()
                    // Markdown can be used with Strife's methods or with string literals like ~~strikethrough~~

                    color = Color.GREEN

                    // Fields can be added like this
                    field("This is a field name (i.e. title)") {
                        "This is the field content (aka value)"
                    }
                    // Inline Fields can be made like this
                    inlineField("This field is inlined") {
                        "It will appear next to other inline fields."
                    }
                    // Or the old way
                    field("This is also inlined", true) {
                        "And it's kinda cool."
                    }
                    // Fields can also be manually added (but this is less cool)
                    fields.add(
                        EmbedBuilder.FieldBuilder(
                            "This FieldBuilder was made and added manually",
                            "And it's lame",
                            false
                        )
                    )

                    // Set the thumbnail (the smaller image in the upper right of the embed)
                    thumbnail(StrifeInfo.logoUri)

                    // Set the large image at the bottom of the embed
                    image(context.selfUser.getAvatar().uri)


                }
            } else if (message.content() == "!edit") {
                // Embeds can also be saved for later!
                val savedEmbed = embed {

                    author { name = "Strife" }
                    title("This embed message was edited!")
                    description = """
                        When editing a Message with a new embed, the old embed is removed and replaced with the new one.
                    """.trimIndent()
                    // Set the thumbnail (the smaller image in the upper right of the embed)
                    thumbnail(StrifeInfo.logoUri)

                    // Set the footer at the bottom of the embed
                    footer {
                        text = "This post was made by Strife Gang"
                        imgUrl = StrifeInfo.logoUri
                        timestamp = message.createdAt
                    }
                }
                embedMessage?.edit("This Message was edited!", savedEmbed)
            }
        }
    }
}
