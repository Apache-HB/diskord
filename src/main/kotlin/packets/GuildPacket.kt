package com.serebit.diskord.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.channels.Channel

internal data class GuildPacket(
    override val id: Snowflake,
    val name: String,
    val roles: List<Role>,
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
    val emojis: List<EmotePacket>,
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
    val voice_states: List<VoiceStatePacket>,
    val members: List<MemberPacket>,
    val channels: List<Channel>,
    val presences: List<PresencePacket>
): EntityPacket
