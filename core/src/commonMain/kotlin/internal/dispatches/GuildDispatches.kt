package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.*
import kotlinx.serialization.Serializable

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildCreateEvent(context, d)
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildUpdatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildUpdateEvent(context, d)
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildDeleteEvent(context, d)
}

@Serializable
internal class GuildBanAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildBanAddEvent(context, d)

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildBanRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildBanRemoveEvent(context, d)

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildMemberAdd(override val s: Int, override val d: MemberPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildMemberJoinEvent(context, d)
}

@Serializable
internal class GuildMemberRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildMemberLeaveEvent(context, d)

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}
