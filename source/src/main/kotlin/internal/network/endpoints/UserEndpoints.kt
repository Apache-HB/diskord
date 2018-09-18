package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.UserPacket
import org.http4k.core.Method

internal object GetSelfUser : Endpoint<UserPacket>(Method.GET, "/users/@me")

internal class GetUser(userId: Long) : Endpoint<UserPacket>(Method.GET, "/users/$userId")
