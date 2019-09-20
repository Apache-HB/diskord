package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.RemoveCacheData
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.data.toBitSet
import com.serebit.strife.internal.entitydata.GuildRoleData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.CreateGuildRolePacket
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

    /** Set the [name][GuildRole.name]. Returns `true` if successful *Requires [Permission.ManageRoles].* */
    suspend fun setName(name: String): Boolean = context.requester.sendRequest(
        Route.ModifyGuildRole(guildId, id, CreateGuildRolePacket(name))
    ).status.isSuccess()

    /**
     * Set the [permissions] of this GuildRole's [permissions][GuildRole.permissions], this will overwrite any existing
     * permissions with the new ones. Returns `true` if successful. *Requires [Permission.ManageRoles].*
     */
    suspend fun setPermissions(permissions: Collection<Permission>): Boolean {
        return context.requester.sendRequest(
            Route.ModifyGuildRole(guildId, id, CreateGuildRolePacket(permissions = permissions.toBitSet()))
        ).status.isSuccess()
    }

    /**
     * Set the [color][GuildRole.color] of this [GuildRole]. Returns `true` if successfully set.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun setColor(color: Color): Boolean = context.requester.sendRequest(
        Route.ModifyGuildRole(guildId, id, CreateGuildRolePacket(color = color.rgb))
    ).status.isSuccess()

    /**
     * Set whether this [GuildRole] should be displayed separately in the sidebar. Returns `true` if set successfully.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun setHoisted(isHoisted: Boolean) : Boolean = context.requester.sendRequest(
        Route.ModifyGuildRole(guildId, id, CreateGuildRolePacket(hoist = isHoisted))
    ).status.isSuccess()

    /**
     * Set whether or not this role can be mentioned in chat. Returns `true` if set successfully.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun setMentionable(mentionable: Boolean) : Boolean = context.requester.sendRequest(
        Route.ModifyGuildRole(guildId, id, CreateGuildRolePacket(mentionable = mentionable))
    ).status.isSuccess()

    /**
     * Set the Role's display [position][GuildRole.position].
     * Returns `true` on success. *Requires [Permission.ManageRoles].*
     */
    suspend fun setPosition(position: Int) = context.requester.sendRequest(
        Route.ModifyGuildRolePosition(guildId, this.id, position)
    ).status.isSuccess()

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

/**
 * Raise the [position][GuildRole.position] at which the Role is displayed in the sidebar by [raiseBy] steps
 * (defaults to `1`). Returns `true` if the position was successfully changed.
 */
suspend fun GuildRole.raise(raiseBy: Int = 1) : Boolean {
    var k = (position - raiseBy)
    if (k < 0) k = 0
    return setPosition(k)
}

/**
 * Lower the [position][GuildRole.position] at which the Role is displayed in the sidebar by [lowerBy] steps
 * (defaults to `1`). Returns `true` if the position was successfully changed.
 */
suspend fun GuildRole.lower(lowerBy: Int = 1) : Boolean {
    var k = (position + lowerBy)
    if (k < 0) k = 0
    return setPosition(k)
}

/**
 * Display this [GuildRole] separately in the sidebar. Returns `true` if successfully hoisted.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.hoist() : Boolean = isHoisted || setHoisted(true)

/**
 * Hide this [GuildRole] from the sidebar. Returns `true` if successfully hidden.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.unHoist() : Boolean = !isHoisted || setHoisted(false)

/**
 * Add [permissions] to this GuildRole's [permissions][GuildRole.permissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.addPermissions(vararg permissions: Permission) = addPermissions(permissions.toList())

/**
 * Add [permissions] to this GuildRole's [permissions][GuildRole.permissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.addPermissions(permissions: Collection<Permission>): Boolean =
    setPermissions(this.permissions + permissions)

/**
 * Remove [permissions] from this GuildRole's [permissions][GuildRole.permissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.removePermissions(vararg permissions: Permission) = removePermissions(permissions.toList())

/**
 * Remove [permissions] from this GuildRole's [permissions][GuildRole.permissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.removePermissions(permissions: Collection<Permission>): Boolean =
    setPermissions(this.permissions - permissions)

/**
 * Set the [permissions] of this GuildRole's [permissions][GuildRole.permissions], this will overwrite any existing
 * permissions with the new ones. Returns `true` if successful. *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.setPermissions(vararg permissions: Permission): Boolean = setPermissions(permissions.toList())
