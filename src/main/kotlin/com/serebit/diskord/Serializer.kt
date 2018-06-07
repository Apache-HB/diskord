package com.serebit.diskord

import com.github.salomonbrys.kotson.DeserializerArg
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonDeserializer
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.serebit.diskord.entities.ChannelCategory
import com.serebit.diskord.entities.DmChannel
import com.serebit.diskord.entities.GroupDmChannel
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.GuildTextChannel
import com.serebit.diskord.entities.GuildVoiceChannel
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.TextChannel
import com.serebit.diskord.entities.TextChannelType
import com.serebit.diskord.entities.UnknownChannel
import com.serebit.diskord.entities.User
import com.serebit.diskord.gateway.Payload

internal object Serializer {
    private val gson: Gson = GsonBuilder().apply {
        serializeNulls()
        registerTypeAdapter(Payload.Dispatch.deserializer)
        registerTypeAdapter(jsonDeserializer { (json, _, context) ->
            when (json["type"].asInt) {
                0, 1, 3 -> context.deserialize<TextChannel>(json)
                2 -> context.deserialize<GuildVoiceChannel>(json)
                4 -> context.deserialize<ChannelCategory>(json)
                else -> context.deserialize<UnknownChannel>(json)
            }
        })
        register { (json, _, context) ->
            when (TextChannelType.values().first { it.value == json["type"].asInt }) {
                TextChannelType.GUILD_TEXT -> context.deserialize<GuildTextChannel>(json)
                TextChannelType.DM -> context.deserialize<DmChannel>(json)
                TextChannelType.GROUP_DM -> context.deserialize<GroupDmChannel>(json)
            }
        }
        register { User(it.context.deserialize(it.json)) }
        register { Guild(it.context.deserialize(it.json)) }
        register { GuildTextChannel(it.context.deserialize(it.json)) }
        register { DmChannel(it.context.deserialize(it.json)) }
        register { GroupDmChannel(it.context.deserialize(it.json)) }
        register { GuildVoiceChannel(it.context.deserialize(it.json)) }
        register { ChannelCategory(it.context.deserialize(it.json)) }
        register { UnknownChannel(it.context.deserialize(it.json)) }
        register { Message(it.context.deserialize(it.json)) }
        register { Role(it.context.deserialize(it.json)) }
    }.create()

    inline fun <reified T : Any> fromJson(json: String) = gson.fromJson<T>(json)

    fun toJson(src: Any): String = gson.toJson(src)

    private inline fun <reified T : Any> GsonBuilder.register(noinline deserializer: (DeserializerArg) -> T) =
        registerTypeAdapter(jsonDeserializer(deserializer))
}
