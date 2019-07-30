package com.serebit.strife.internal.packets

import com.serebit.strife.entities.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule

private fun PolymorphicModuleBuilder<Any>.applyGuildChannelSerializers() {
    GuildTextChannelPacket::class with GuildTextChannelPacket.serializer()
    GuildNewsChannelPacket::class with GuildNewsChannelPacket.serializer()
    GuildStoreChannelPacket::class with GuildStoreChannelPacket.serializer()
    GuildVoiceChannelPacket::class with GuildVoiceChannelPacket.serializer()
    GuildChannelCategoryPacket::class with GuildChannelCategoryPacket.serializer()
}

/** An [EntityPacket] with information about a [Channel][Channel]. */
internal interface ChannelPacket : EntityPacket {
    companion object {
        @Suppress("Unchecked_Cast")
        val polymorphicSerializer = PolymorphicSerializer(ChannelPacket::class) as KSerializer<ChannelPacket>

        val serializerModule = SerializersModule {
            polymorphic(ChannelPacket::class) {
                applyGuildChannelSerializers()
                DmChannelPacket::class with DmChannelPacket.serializer()
            }
        }
    }
}

/** A [ChannelPacket] for [TextChannels][TextChannel]. */
internal interface TextChannelPacket : ChannelPacket {
    /** The [id][Message.id] of the last [Message] sent in this [TextChannel]. */
    val last_message_id: Long?
    /** The timestamp of the last time a [Message] was pinned in this [TextChannel]. */
    val last_pin_timestamp: String?
}

/** A [ChannelPacket] for [GuildTextChannel] and [GuildVoiceChannel]. */
internal interface GuildChannelPacket : ChannelPacket {
    /** The [id][Guild.id] of the [ChannelPacket]. */
    val guild_id: Long?
    /** The positioning of the [Channel] in the [Guild]'s menu. */
    val position: Short
    val name: String
    val nsfw: Boolean
    val permission_overwrites: List<PermissionOverwritePacket>
    val parent_id: Long?

    companion object {
        @Suppress("Unchecked_Cast")
        val polymorphicSerializer = PolymorphicSerializer(GuildChannelPacket::class) as KSerializer<GuildChannelPacket>

        val serializerModule = SerializersModule {
            polymorphic(GuildChannelPacket::class) {
                applyGuildChannelSerializers()
            }
        }
    }
}

@Serializable
@SerialName(GuildTextChannel.typeCode.toString())
internal data class GuildTextChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    val topic: String? = null,
    override val nsfw: Boolean = false,
    override val last_message_id: Long? = null,
    override val parent_id: Long? = null,
    override val last_pin_timestamp: String? = null,
    val rate_limit_per_user: Short? = null
) : TextChannelPacket, GuildChannelPacket

@Serializable
@SerialName(GuildNewsChannel.typeCode.toString())
internal data class GuildNewsChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    val topic: String? = null,
    override val nsfw: Boolean = false,
    override val last_message_id: Long? = null,
    override val parent_id: Long? = null,
    override val last_pin_timestamp: String? = null
) : TextChannelPacket, GuildChannelPacket

@Serializable
@SerialName(GuildStoreChannel.typeCode.toString())
internal data class GuildStoreChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val nsfw: Boolean = false,
    override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
@SerialName(GuildVoiceChannel.typeCode.toString())
internal data class GuildVoiceChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val nsfw: Boolean = false,
    val bitrate: Int,
    val user_limit: Byte,
    override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
@SerialName(GuildChannelCategory.typeCode.toString())
internal data class GuildChannelCategoryPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val name: String,
    override val parent_id: Long? = null,
    override val nsfw: Boolean = false,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>
) : GuildChannelPacket

@Serializable
@SerialName(DmChannel.typeCode.toString())
internal data class DmChannelPacket(
    override val id: Long,
    val recipients: List<UserPacket>,
    override val last_message_id: Long? = null,
    override val last_pin_timestamp: String? = null
) : TextChannelPacket
