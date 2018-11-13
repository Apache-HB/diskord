package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.UserPacket
import io.ktor.http.HttpMethod

internal object GetSelfUser : Endpoint.Object<UserPacket>(
    HttpMethod.Get, "users/@me", UserPacket.serializer()
)

internal class GetUser(userId: Long) : Endpoint.Object<UserPacket>(
    HttpMethod.Get, "users/$userId", UserPacket.serializer()
)
