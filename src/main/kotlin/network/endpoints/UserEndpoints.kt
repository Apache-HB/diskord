@file:JvmName("UserEndpoints")

package com.serebit.diskord.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

internal class GetUser(userId: Snowflake) : Endpoint.Get<User>("/users/$userId")
