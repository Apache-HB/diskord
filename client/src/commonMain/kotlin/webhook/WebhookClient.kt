package com.serebit.strife.webhook
import com.serebit.strife.BotClient
import com.serebit.strife.entities.Message

/**
 * A client to interact with webhooks without getting them from a [BotClient]. Useful for web services and bots that
 * store webhook IDs and tokens to execute them when needed.
 *
 * @property webhookID The ID of the webhook.
 * @property webhookToken The token of the webhook.
 */
class WebhookClient(private val webhookID: Long, private val webhookToken: String) {
    // TODO
    suspend fun getWebhook(): Webhook {

    }

    // TODO
    suspend fun modifyWebhook(): Webhook {

    }

    // TODO
    suspend fun deleteWebhook(): Webhook {

    }

    // TODO
    suspend fun executeWebhook(): Message {
        
    }
}