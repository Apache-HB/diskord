package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.internal.packets.UserPacket
import org.http4k.core.Method

internal class GetUser(userId: Snowflake) : Endpoint<UserPacket>(Method.GET, "/users/$userId")
