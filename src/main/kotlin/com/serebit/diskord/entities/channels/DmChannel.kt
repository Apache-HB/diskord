package com.serebit.diskord.entities.channels

import com.fasterxml.jackson.annotation.JsonCreator
import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

class DmChannel private constructor(
    override val id: Snowflake,
    recipients: List<User>,
    last_message_id: Snowflake
) : TextChannel {
    var recipients: List<User> = recipients
        private set

    companion object {
        @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
        fun create(id: Snowflake, recipients: List<User>, last_message_id: Snowflake): DmChannel =
            EntityCache.find<DmChannel>(id)?.also {
                it.recipients = recipients
            } ?: EntityCache.cache(DmChannel(id, recipients, last_message_id))
    }
}
