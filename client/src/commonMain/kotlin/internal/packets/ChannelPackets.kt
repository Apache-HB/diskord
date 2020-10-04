package com.serebit.strife.internal.packets

import com.serebit.strife.entities.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** An [EntityPacket] with information about a [Channel][Channel]. */
internal interface ChannelPacket : EntityPacket {
    companion object {
        @Suppress("Unchecked_Cast")
        val polymorphicSerializer = PolymorphicSerializer(ChannelPacket::class)

        val serializerModule = SerializersModule {
            fun PolymorphicModuleBuilder<GuildChannelPacket>.registerGuildChannelSubclasses() {
                subclass(GuildTextChannelPacket::class, GuildTextChannelPacket.serializer())
                subclass(GuildNewsChannelPacket::class, GuildNewsChannelPacket.serializer())
                subclass(GuildStoreChannelPacket::class, GuildStoreChannelPacket.serializer())
                subclass(GuildVoiceChannelPacket::class, GuildVoiceChannelPacket.serializer())
                subclass(GuildChannelCategoryPacket::class, GuildChannelCategoryPacket.serializer())
            }

            polymorphic(ChannelPacket::class) {
                subclass(DmChannelPacket::class, DmChannelPacket.serializer())
                registerGuildChannelSubclasses()
            }

            polymorphic(GuildChannelPacket::class) {
                registerGuildChannelSubclasses()
            }

            polymorphic(GuildMessageChannelPacket::class) {
                subclass(GuildTextChannelPacket::class, GuildTextChannelPacket.serializer())
                subclass(GuildNewsChannelPacket::class, GuildNewsChannelPacket.serializer())
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

/** A [ChannelPacket] for [GuildChannels][GuildChannel]. */
internal interface GuildChannelPacket : ChannelPacket {
    /** The [id][Guild.id] of the [ChannelPacket]. */
    val guild_id: Long?

    /** The positioning of the [Channel] in the [Guild]'s menu. */
    val position: Short
    val name: String
    val permission_overwrites: List<PermissionOverwritePacket>
    val parent_id: Long?

    companion object {
        @Suppress("Unchecked_Cast")
        val polymorphicSerializer = PolymorphicSerializer(GuildChannelPacket::class) as KSerializer<GuildChannelPacket>
    }
}

/** A [ChannelPacket] for [GuildMessageChannel]. */
internal interface GuildMessageChannelPacket : TextChannelPacket, GuildChannelPacket {
    val nsfw: Boolean
    val topic: String?
}

@Serializable
@SerialName(0.toString())
internal data class GuildTextChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val topic: String? = null,
    override val nsfw: Boolean = false,
    override val last_message_id: Long? = null,
    override val parent_id: Long? = null,
    override val last_pin_timestamp: String? = null,
    val rate_limit_per_user: Short? = null
) : GuildMessageChannelPacket

@Serializable
@SerialName(5.toString())
internal data class GuildNewsChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val topic: String? = null,
    override val nsfw: Boolean = false,
    override val last_message_id: Long? = null,
    override val parent_id: Long? = null,
    override val last_pin_timestamp: String? = null
) : GuildMessageChannelPacket

@Serializable
@SerialName(6.toString())
internal data class GuildStoreChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
@SerialName(2.toString())
internal data class GuildVoiceChannelPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    val bitrate: Int,
    val user_limit: Byte,
    override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
@SerialName(4.toString())
internal data class GuildChannelCategoryPacket(
    override val id: Long,
    override var guild_id: Long? = null,
    override val name: String,
    override val parent_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>
) : GuildChannelPacket

@Serializable
@SerialName(1.toString())
internal data class DmChannelPacket(
    override val id: Long,
    val recipients: List<UserPacket>,
    override val last_message_id: Long? = null,
    override val last_pin_timestamp: String? = null
) : TextChannelPacket
