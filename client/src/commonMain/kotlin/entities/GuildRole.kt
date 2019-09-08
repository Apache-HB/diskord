package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.RemoveCacheData
import com.serebit.strife.internal.entitydata.GuildRoleData
import com.serebit.strife.internal.network.Route
import io.ktor.http.isSuccess

/**
 * Represents a role in a Discord server. Roles are used to group users,
 * and those groups can be given specific name colors and permissions.
 */
class GuildRole internal constructor(private val data: GuildRoleData) : Entity, Mentionable {
    override val id: Long = data.id
    override val context: BotClient = data.context
    override val asMention: String get() = id.asMention(MentionType.ROLE)
    /** The name of this role. */
    val name: String get() = data.name
    /** The position of this role in its parent guild's role hierarchy. */
    val position get() = data.position
    /** The color assigned to this role as a Java color. */
    val color get() = data.color
    /** The permissions assigned to this role. */
    val permissions get() = data.permissions
    /** Whether or not this role appears as its own section in the sidebar. */
    val isHoisted get() = data.isHoisted
    /** Whether or not this role is managed by an external source (e.g. Patreon or a Discord bot). */
    val isManaged get() = data.isManaged
    /** Whether or not this role can be mentioned in chat. */
    val isMentionable get() = data.isMentionable
    /** The ID of the [Guild] that this role belongs to. */
    val guildId: Long get() = data.guildId

    /** Get the [Guild] that this role belongs to. */
    suspend fun getGuild(): Guild = context.cache.getGuildData(data.guildId)!!.lazyEntity

    /** Delete this role. Exceptions may occur if this object is referenced after deletion. */
    suspend fun delete(): Boolean = context.requester.sendRequest(Route.DeleteGuildRole(guildId, id))
        .status
        .isSuccess()
        .also {
            context.cache.remove(RemoveCacheData.GuildRole(id))
            context.cache.getGuildData(guildId)?.roles?.remove(id)
        }

    /** Checks if this guild role is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildRole && other.id == id
}
