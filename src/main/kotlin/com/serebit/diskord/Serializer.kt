package com.serebit.diskord

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.gsonTypeToken
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
import com.serebit.diskord.entities.TextChannel
import com.serebit.diskord.entities.TextChannelType
import com.serebit.diskord.entities.UnknownChannel
import com.serebit.diskord.entities.User
import com.serebit.diskord.gateway.Payload
import java.lang.reflect.Type

internal object Serializer {
    private val gson: Gson = GsonBuilder().apply {
        registerTypeAdapter(Payload.Dispatch.deserializer)
        registerTypeAdapter(jsonDeserializer { (json, _, context) ->
            when (json["type"].asInt) {
                0, 1, 3 -> context.deserialize<TextChannel>(json)
                2 -> context.deserialize<GuildVoiceChannel>(json)
                4 -> context.deserialize<ChannelCategory>(json)
                else -> context.deserialize<UnknownChannel>(json)
            }
        })
        registerTypeAdapter(jsonDeserializer { (json, _, context) ->
            when (TextChannelType.values().first { it.value == json["type"].asInt }) {
                TextChannelType.GUILD_TEXT -> context.deserialize<GuildTextChannel>(json)
                TextChannelType.DM -> context.deserialize<DmChannel>(json)
                TextChannelType.GROUP_DM -> context.deserialize<GroupDmChannel>(json)
            }
        })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(User(it.context.deserialize(it.json))) })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(Guild(it.context.deserialize(it.json))) })
        registerTypeAdapter(jsonDeserializer {
            EntityCacher.cache(GuildTextChannel(it.context.deserialize(it.json)))
        })
        registerTypeAdapter(jsonDeserializer {
            EntityCacher.cache(DmChannel(it.context.deserialize(it.json)))
        })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(GroupDmChannel(it.context.deserialize(it.json))) })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(GuildVoiceChannel(it.context.deserialize(it.json))) })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(ChannelCategory(it.context.deserialize(it.json))) })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(UnknownChannel(it.context.deserialize(it.json))) })
        registerTypeAdapter(jsonDeserializer { EntityCacher.cache(Message(it.context.deserialize(it.json))) })
    }.create()

    inline fun <reified T : Any> fromJson(json: String) = fromJson<T>(json, gsonTypeToken<T>())

    fun <T : Any> fromJson(json: String, type: Type): T = gson.fromJson(json, type)
}
