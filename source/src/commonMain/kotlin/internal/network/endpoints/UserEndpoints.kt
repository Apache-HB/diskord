package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.UserPacket
import io.ktor.http.HttpMethod

internal object GetSelfUser : Endpoint<UserPacket>(HttpMethod.Get, "/users/@me")

internal class GetUser(userId: Long) : Endpoint<UserPacket>(HttpMethod.Get, "/users/$userId")
