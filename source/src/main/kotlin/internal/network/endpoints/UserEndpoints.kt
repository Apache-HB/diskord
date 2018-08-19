package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User
import org.http4k.core.Method

internal class GetUser(userId: Snowflake) : Endpoint<User>(Method.GET, "/users/$userId")
