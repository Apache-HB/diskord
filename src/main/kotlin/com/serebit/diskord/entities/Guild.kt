package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCache
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.DiscordEntityData
import com.serebit.diskord.data.EmoteData
import com.serebit.diskord.data.VoiceStateData

class Guild internal constructor(data: Data) : DiscordEntity {
    override val id: Long = data.id
    val channels: List<Channel> = data.channels

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake,
        val name: String,
        val icon: String?,
        val splash: String?,
        val owner: Boolean?,
        val owner_id: Snowflake,
        val permissions: BitSet?,
        val region: String,
        val afk_channel_id: Snowflake?,
        val afk_timeout: Int,
        val embed_enabled: Boolean?,
        val embed_channel_id: Snowflake,
        val verification_level: Int,
        val default_message_notifications: Int,
        val explicit_content_filter: Int,
        val roles: List<Role>,
        val emojis: List<EmoteData>,
        val features: List<String>,
        val mfa_level: Int,
        val application_id: Snowflake?,
        val widget_enabled: Boolean?,
        val widget_channel_id: Snowflake?,
        val system_channel_id: Snowflake?,
        val joined_at: IsoTimestamp,
        val large: Boolean?,
        val unavailable: Boolean?,
        val member_count: Int?,
        val voice_states: List<VoiceStateData>,
        val members: List<MemberData>,
        val channels: List<Channel>,
        val presences: List<PresenceData>
    ) : DiscordEntityData {
        data class MemberData(
            val user: User,
            val nick: String?,
            val roles: List<Snowflake>,
            val joined_at: IsoTimestamp,
            val deaf: Boolean,
            val mute: Boolean
        )

        data class PresenceData(
            val user: User,
            val roles: List<Snowflake>,
            val game: User.Data.ActivityData?,
            val guild_id: Snowflake,
            val status: String
        )
    }
}
