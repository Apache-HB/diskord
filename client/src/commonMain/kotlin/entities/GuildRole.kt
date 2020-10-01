package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.RemoveCacheData
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.data.toBitSet
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.CreateGuildRolePacket
import io.ktor.http.isSuccess

/**
 * Represents a role in a Discord server. Roles are used to group users,
 * and those groups can be given specific name colors and permissions.
 *
 * @property guildID The unique [ID][Guild.id] of the [Guild] this [GuildRole] exists in.
 */
class GuildRole internal constructor(override val id: Long, val guildID: Long, override val context: BotClient) :
    Entity, Mentionable {

    private suspend fun getData() = context.obtainGuildRoleData(id, guildID)
        ?: error("Attempted to get data for a nonexistent user with ID $id")

    override suspend fun asMention(): String = id.asMention(MentionType.ROLE)

    /** The name of this role. */
    suspend fun getName(): String = getData().name

    /**
     *  The position of this role in its parent guild's role hierarchy. This Determines where in the
     *  sidebar this role will be displayed, as well as which roles it outranks.
     */
    suspend fun getPosition(): Short = getData().position

    /** The [Color] assigned to this role. */
    suspend fun getColor(): Color = getData().color

    /** The permissions assigned to this role. */
    suspend fun getPermissions(): Set<Permission> = getData().permissions

    /** Whether or not this role appears as its own section in the sidebar. */
    suspend fun isHoisted(): Boolean = getData().isHoisted

    /** Whether or not this role is managed by an external source (e.g. Patreon or a Discord bot). */
    suspend fun isManaged(): Boolean = getData().isManaged

    /** Whether or not this role can be mentioned in chat. */
    suspend fun isMentionable(): Boolean = getData().isMentionable

    /** Get the [Guild] that this role belongs to. */
    suspend fun getGuild(): Guild = context.cache.getGuildData(guildID)!!.lazyEntity

    /** Set the [name][GuildRole.getName]. Returns `true` if successful *Requires [Permission.ManageRoles].* */
    suspend fun setName(name: String): Boolean =
        context.requester.sendRequest(Route.ModifyGuildRole(guildID, id, CreateGuildRolePacket(name)))
            .status.isSuccess()

    /**
     * Set the [permissions] of this GuildRole's [permissions][GuildRole.getPermissions], this will overwrite any existing
     * permissions with the new ones. Returns `true` if successful. *Requires [Permission.ManageRoles].*
     */
    suspend fun setPermissions(permissions: Collection<Permission>): Boolean {
        return context.requester.sendRequest(
            Route.ModifyGuildRole(guildID, id, CreateGuildRolePacket(permissions = permissions.toBitSet()))
        ).status.isSuccess()
    }

    /**
     * Set the [color][GuildRole.getColor] of this [GuildRole]. Returns `true` if successfully set.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun setColor(color: Color): Boolean =
        context.requester.sendRequest(
            Route.ModifyGuildRole(
                guildID,
                id,
                CreateGuildRolePacket(color = color.rgb)
            )
        ).status.isSuccess()

    /**
     * Set whether this [GuildRole] should be displayed separately in the sidebar. Returns `true` if set successfully.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun setHoisted(isHoisted: Boolean): Boolean =
        context.requester.sendRequest(
            Route.ModifyGuildRole(
                guildID,
                id,
                CreateGuildRolePacket(hoist = isHoisted)
            )
        ).status.isSuccess()

    /**
     * Set whether or not this role can be mentioned in chat. Returns `true` if set successfully.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun setMentionable(mentionable: Boolean): Boolean =
        context.requester.sendRequest(
            Route.ModifyGuildRole(guildID, id, CreateGuildRolePacket(mentionable = mentionable))
        ).status.isSuccess()

    /** Set the Role's [position][GuildRole.getPosition]. Returns `true` on success. Requires [Permission.ManageRoles]. */
    suspend fun setPosition(position: Int): Boolean = getGuild().setRolePosition(id, position)

    /**
     * Delete this [GuildRole]. Exceptions may occur if this object is referenced after deletion.
     * If the [GuildRole] insistence is not available, use [Guild.deleteRole].
     */
    suspend fun delete(): Boolean = context.requester.sendRequest(Route.DeleteGuildRole(guildID, id))
        .status
        .isSuccess()
        .also {
            context.cache.remove(RemoveCacheData.GuildRole(id))
            context.cache.getGuildData(guildID)?.roles?.remove(id)
        }

    /**
     * Compares the [getPosition]s of this [GuildRole] and the provided [role].
     * Returns i > 0 if this role outranks the other.
     */
    suspend operator fun compareTo(role: GuildRole) = getPosition() - role.getPosition()

    /** Checks if this guild role is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildRole && other.id == id

}

/**
 * Raise the [position][GuildRole.getPosition] at which the Role is displayed in the sidebar by [raiseBy] steps
 * (defaults to `1`). Returns `true` if the position was successfully changed.
 */
suspend fun GuildRole.raise(raiseBy: Int = 1): Boolean = setPosition(getPosition() + raiseBy)

/**
 * Lower the [position][GuildRole.getPosition] at which the Role is displayed in the sidebar by [lowerBy] steps
 * (defaults to `1`). Returns `true` if the position was successfully changed.
 */
suspend fun GuildRole.lower(lowerBy: Int = 1): Boolean {
    var k = getPosition() - lowerBy
    if (k < 1) k = 1
    return setPosition(k)
}

/**
 * Display this [GuildRole] separately in the sidebar. Returns `true` if successfully hoisted.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.hoist(): Boolean = setHoisted(true)

/**
 * Hide this [GuildRole] from the sidebar. Returns `true` if successfully hidden.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.unHoist(): Boolean = setHoisted(false)

/**
 * Add [permissions] to this GuildRole's [permissions][GuildRole.getPermissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.addPermissions(vararg permissions: Permission): Boolean = addPermissions(permissions.toList())

/**
 * Add [permissions] to this GuildRole's [permissions][GuildRole.getPermissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.addPermissions(permissions: Collection<Permission>): Boolean =
    setPermissions(this.getPermissions() + permissions)

/**
 * Remove [permissions] from this GuildRole's [permissions][GuildRole.getPermissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.removePermissions(vararg permissions: Permission): Boolean =
    removePermissions(permissions.toList())

/**
 * Remove [permissions] from this GuildRole's [permissions][GuildRole.getPermissions]. Returns `true` if successful.
 * *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.removePermissions(permissions: Collection<Permission>): Boolean =
    setPermissions(this.getPermissions() - permissions)

/**
 * Set the [permissions] of this GuildRole's [permissions][GuildRole.getPermissions], this will overwrite any existing
 * permissions with the new ones. Returns `true` if successful. *Requires [Permission.ManageRoles].*
 */
suspend fun GuildRole.setPermissions(vararg permissions: Permission): Boolean = setPermissions(permissions.toList())

/** Removes all [Permission]s from this [GuildRole]. Returns `true` if successful. */
suspend fun GuildRole.clearPermissions(): Boolean = setPermissions(emptyList())
