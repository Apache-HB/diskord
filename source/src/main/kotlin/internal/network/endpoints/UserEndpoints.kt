package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

internal class GetUser(userId: Snowflake) : Endpoint.Get<User>("/users/$userId")
