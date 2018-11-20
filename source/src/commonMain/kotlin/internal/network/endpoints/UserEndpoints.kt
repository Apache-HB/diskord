package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.UserPacket
import io.ktor.http.HttpMethod

internal object GetSelfUser : Endpoint.ObjectData<UserPacket>(
    HttpMethod.Get, "users/@me", UserPacket.serializer()
)

internal class GetUser(userId: Long) : Endpoint.ObjectData<UserPacket>(
    HttpMethod.Get, "users/$userId", UserPacket.serializer()
)
