package com.serebit.strife.botmemory

import com.serebit.strife.BotBuilder
import com.serebit.strife.StrifeInfo
import com.serebit.strife.data.Color
import com.serebit.strife.entities.*

/** Returns the [MemoryFeature] of the given type [M]. */
@Suppress("UNCHECKED_CAST")
fun <M : Memory> BotBuilder.getMemoryFeature(): MemoryFeature<M> =
    features[MemoryFeature.FEATURE_NAME] as? MemoryFeature<M>
        ?: error("BotMemory feature not installed.")

/**
 * Retrieve all [memories][Memory] of type [M] currently held by the bot client.
 *
 * Note: The type [M] must be the same type used to install the provider.
 */
fun <M : Memory> BotBuilder.memories(): Map<Long, M> = getMemoryFeature<M>().memories

/**
 * Retrieve all [memories][Memory] of type [M] with the [MemoryType] of [types] currently held by the bot client.
 *
 * Note: The type [M] must be the same type used to install the [provider.
 */
fun <M : Memory> BotBuilder.memoriesOf(vararg types: MemoryType): Map<Long, M> {
    require(types.isNotEmpty()) { "At least one MemoryType must be specified." }
    return memories<M>().filterValues { it.type in types }
}

/**
 * Retrieve the [Memory] of type [M] associated with the given [id] and run the lambda [scope] with the memory.
 * Returns the modified memory or null if one was not found at the given key.
 *
 * Note: [M] must be the same type used to install the provider.
 */
suspend fun <M : Memory> BotBuilder.memory(id: Long?, scope: suspend M.() -> Unit): M? =
    id?.let { getMemoryFeature<M>().modifyMemory(it, scope) }

/** Manually create a [Memory] of type [M] to be stored at the given [key]. Returns the new [Memory]. */
suspend fun <M : Memory> BotBuilder.remember(key: Long, init: suspend () -> M): M =
    getMemoryFeature<M>().remember(key, init())

/** Manually remove a [Memory] of type [M] from the given [key]. Returns the forgotten memory or `null` if not found. */
suspend fun <M : Memory> BotBuilder.forget(key: Long): M? = getMemoryFeature<M>().forget(key)

/**
 * Sends the number of currently held [memories][Memory] and all memory instances in an [embed][EmbedBuilder].
 * If all memories cannot be displayed, they will not be. Returns the sent [Message] if successful.
 */
suspend fun BotBuilder.memoryDebug(channel: TextChannel): Message? = channel.send {
    val mems = StringBuilder()
    val distibution = mutableMapOf<String, Int>()
    val memories = memories<Memory>()
    memories.values.forEach { m ->
        distibution[m.type.name] = distibution[m.type.name]?.plus(1) ?: 1
        if (mems.length < EmbedBuilder.DESCRIPTION_MAX) mems.append("\n$m")
    }
    title("${memories.size} Memories")
    description = if (mems.length > EmbedBuilder.DESCRIPTION_MAX) "*Too many memories to display*" else mems.toString()
    field("Distribution") {
        val s = StringBuilder()
        distibution.toList()
            .sortedByDescending { it.second }
            .forEach {
                s.append("""${it.first}: ${it.second}  """)
                    .append("""(${((it.second / memories.size.toDouble()) * 100).toInt()}%)""")
                    .append("\n")
            }
        s.toString()
    }
    footer {
        text = "Made using Strife"
        imgUrl = StrifeInfo.logoUri
    }
    color = Color(memories.size * 100)
}