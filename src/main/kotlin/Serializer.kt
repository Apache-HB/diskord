package com.serebit.diskord

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.serebit.diskord.data.entities.channels.*
import com.serebit.diskord.entities.channels.*

internal object Serializer {
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        registerModule(KotlinModule())
        deserializer { (json, mapper) ->
            when (json["type"].asInt()) {
                GuildTextChannel.typeCode, DmChannel.typeCode, GroupDmChannel.typeCode ->
                    mapper.readValue<TextChannel>(json.toString())
                GuildVoiceChannel.typeCode -> mapper.readValue<GuildVoiceChannel>(json.toString())
                ChannelCategory.typeCode -> mapper.readValue<ChannelCategory>(json.toString())
                else -> null
            }
        }
        deserializer { (json, mapper) ->
            when (json["type"].asInt()) {
                GuildTextChannel.typeCode -> mapper.readValue<GuildTextChannel>(json.toString())
                DmChannel.typeCode -> mapper.readValue<DmChannel>(json.toString())
                GroupDmChannel.typeCode -> mapper.readValue<GroupDmChannel>(json.toString())
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
