package com.serebit.diskord

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.serebit.diskord.entities.channels.ChannelCategory
import com.serebit.diskord.entities.channels.DmChannel
import com.serebit.diskord.entities.channels.GroupDmChannel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.entities.channels.TextChannel

internal object Serializer {
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
        registerModule(KotlinModule())
        deserializer { (json, mapper) ->
            when (json["type"].asInt()) {
                0, 1, 3 -> mapper.readValue<TextChannel>(json.toString())
                2 -> mapper.readValue<GuildVoiceChannel>(json.toString())
                4 -> mapper.readValue<ChannelCategory>(json.toString())
                else -> null
            }
        }
        deserializer { (json, mapper) ->
            when (json["type"].asInt() ) {
                0 -> mapper.readValue<GuildTextChannel>(json.toString())
                1 -> mapper.readValue<DmChannel>(json.toString())
                3 -> mapper.readValue<GroupDmChannel>(json.toString())
                else -> null
            }
        }
    }

    inline fun <reified T : Any> fromJson(json: String): T = objectMapper.readValue(json)

    fun toJson(src: Any): String = objectMapper.writeValueAsString(src)

    private inline fun <reified T : Any?> ObjectMapper.deserializer(crossinline deserializer: (DeserializerArg) -> T) =
        registerModule(SimpleModule().apply {
            addDeserializer(T::class.java, object : JsonDeserializer<T>() {
                override fun deserialize(parser: JsonParser, context: DeserializationContext): T =
                    deserializer(DeserializerArg(parser.codec.readTree(parser), objectMapper))
            })
        })

    private data class DeserializerArg(val json: JsonNode, val mapper: ObjectMapper)
}
