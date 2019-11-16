package com.serebit.strife.entities

import com.serebit.strife.internal.entitydata.ApplicationInfoData

/**
 * [ApplicationInfo] describes a discord application (a game, a bot, or any other app).
 * Right now, it is only possible to query this information for the current bot user.
 */
class ApplicationInfo internal constructor(data: ApplicationInfoData) : Entity {
    override val id = data.id
    override val context = data.context

    val name = data.name
    val description = data.description
    val public = data.public
    val requireCodeGrant = data.requireCodeGrant
    val owner = User(data.owner)
}
