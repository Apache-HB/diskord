package com.serebit.diskord.utils

class Converter {

    const val administrator = 8
    const val view_audit_log = 128
    const val manage_server = 32
    const val manage_roles = 268435456
    const val manage_channels = 16
    const val kick_members = 2
    const val ban_members = 4
    const val create_instant_invite = 1
    const val change_nickname = 67108864
    const val manage_nicknames = 134217728
    const val manage_emojis = 1073741824
    const val manage_webhooks = 536870912
    const val view_channels = 1024

    fun roleConverter(snowflake: Long): RolePermissions {
        return RolePermissions(
            GeneralPermissions(
                snowflake and 8 == 8,
                snowflake and 128 == 128,
                snowflake and 32 == 32
            ),
            TextPermissions(

            ),
            VoicePermissions(

            )
    )
    //TODO: rigure out how to convert snowflakes to roles
}

}