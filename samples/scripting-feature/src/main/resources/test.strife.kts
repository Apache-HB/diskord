import com.serebit.strife.entities.reply
import com.serebit.strife.onMessageCreate
import com.serebit.strife.onReady

onReady { println("Connected to Discord!") }

// On "!ping" messages, send PONG!
onMessageCreate {
    if (message.getContent() == "!ping")
        message.reply("Pong! Latency is ${context.gatewayLatency}ms.")
}
