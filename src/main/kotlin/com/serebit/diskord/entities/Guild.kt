package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCache
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.BasicUser
import com.serebit.diskord.data.EmoteData
import com.serebit.diskord.data.Permission
import com.serebit.diskord.data.VoiceStateData

class Guild internal constructor(
    override val id: Snowflake,
    val name: String,
    val roles: List<Role>,
    icon: String?,
    splash: String?,
    owner: Boolean?,
    owner_id: Snowflake,
    permissions: BitSet?,
    region: String,
    afk_channel_id: Snowflake?,
    afk_timeout: Int,
    embed_enabled: Boolean?,
    embed_channel_id: Snowflake,
    verification_level: Int,
    default_message_notifications: Int,
    explicit_content_filter: Int,
    emojis: List<EmoteData>,
    features: List<String>,
    mfa_level: Int,
    application_id: Snowflake?,
    widget_enabled: Boolean?,
    widget_channel_id: Snowflake?,
    system_channel_id: Snowflake?,
    joined_at: IsoTimestamp,
    large: Boolean?,
    unavailable: Boolean?,
    member_count: Int?,
    voice_states: List<VoiceStateData>,
    members: List<MemberData>,
    val channels: List<Channel>,
    presences: List<PresenceData>
) : DiscordEntity {
    val owner: User = EntityCache.find(owner_id)!!
    val permissions = Permission.from(permissions ?: 0)

    init {
        EntityCache.cache(this)
    }

    internal data class MemberData(
        val user: User,
        val nick: String?,
        val roles: List<Snowflake>,
        val joined_at: IsoTimestamp,
        val deaf: Boolean,
        val mute: Boolean
    )

    internal data class PresenceData(
        val user: BasicUser,
        val roles: List<Snowflake>?,
        val game: User.ActivityData?,
        val guild_id: Snowflake?,
        val status: String?
    )
}
