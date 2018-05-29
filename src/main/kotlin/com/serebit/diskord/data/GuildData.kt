package com.serebit.diskord.data

import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCacher
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Guild

internal data class GuildData(
    val id: Snowflake,
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
    val roles: List<RoleData>,
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
    val channels: List<ChannelData>,
    val presences: List<PresenceData>
) {
    data class MemberData(
        val user: UserData,
        val nick: String?,
        val roles: List<Snowflake>,
        val joined_at: IsoTimestamp,
        val deaf: Boolean,
        val mute: Boolean
    )

    data class PresenceData(
        val user: UserData,
        val roles: List<Snowflake>,
        val game: UserData.ActivityData?,
        val guild_id: Snowflake,
        val status: String
    )

    fun toGuild() = EntityCacher.cache(Guild(id.toLong()))
}
