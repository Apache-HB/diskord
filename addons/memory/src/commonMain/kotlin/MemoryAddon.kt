package com.serebit.strife.botmemory

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.logkat.debug
import com.serebit.strife.BotBuilder
import com.serebit.strife.BotAddon
import com.serebit.strife.BotAddonProvider
import com.serebit.strife.entities.*
import com.serebit.strife.events.ChannelCreateEvent
import com.serebit.strife.events.ChannelDeleteEvent
import com.serebit.strife.events.GuildCreateEvent
import com.serebit.strife.events.GuildDeleteEvent
import com.serebit.strife.onEvent
import kotlinx.serialization.Serializable
import kotlin.collections.set

/** A [Memory] is used by the [MemoryAddon] to store any custom information associated with a [Guild] or [User]. */
interface Memory {
    /** The [MemoryType] of this memory. */
    val type: MemoryType

    /**
     * A default [Memory] implementation which holds only a [prefix]
     * @property prefix The command prefix.
     */
    class Default(override val type: MemoryType, var prefix: String = "!") : Memory
}

/**
 * A [MemoryType] is useful for debugging and stats collection.
 * @property name The name of the [MemoryType]
 */
@Serializable
open class MemoryType(val name: String) {
    /** A [User] MemoryType. */
    object User : MemoryType("user")

    /** A [Message] MemoryType. */
    object Message : MemoryType("message")

    /** A [Guild] MemoryType. */
    object Guild : MemoryType("guild")

    /** A [GuildTextChannel] MemoryType. */
    object GuildTextChannel : MemoryType("text channel (guild)")

    /** A [GuildVoiceChannel] MemoryType. */
    object GuildVoiceChannel : MemoryType("voice channel (guild)")

    /** A MemoryType for any custom types. */
    class Other(name: String) : MemoryType(name)

    /** Returns the [name]. */
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean = other is MemoryType && other.name == this.name

    override fun hashCode(): Int = name.hashCode()
}

/** The [MemoryProvider] is used by a [MemoryAddon] to produce new [memories][Memory] from a specific [Entity]. */
open class MemoryProvider<M : Memory> {
    /** The provider for a [Memory] generated from a [Guild]. */
    var guild: (suspend (Guild) -> M)? = null
        protected set

    /** The provider for a [Memory] generated from a [User]'s [DmChannel]. */
    var user: (suspend (User) -> M)? = null
        protected set

    /** The provider for a [Memory] generated from a [Message]. */
    var message: (suspend (Message) -> M)? = null
        protected set

    /** The provider for a [Memory] generated from a [GuildTextChannel]. */
    var textChannel: (suspend (GuildTextChannel) -> M)? = null
        protected set

    /** The provider for a [Memory] generated from a [GuildVoiceChannel]. */
    var voiceChannel: (suspend (GuildVoiceChannel) -> M)? = null
        protected set

    /** Set the [guild] memory provider function. */
    open fun guild(provider: suspend (Guild) -> M) {
        guild = provider
    }

    /** Set the [user] memory provider function. */
    open fun user(provider: suspend (User) -> M) {
        user = provider
    }

    /** Set the [message] memory provider function. */
    open fun message(provider: suspend (Message) -> M) {
        message = provider
    }

    /** Set the [textChannel] memory provider function. */
    open fun textChannel(provider: suspend (TextChannel) -> M) {
        textChannel = provider
    }

    /** Set the [voiceChannel] memory provider function. */
    open fun voiceChannel(provider: suspend (GuildVoiceChannel) -> M) {
        voiceChannel = provider
    }

}

/** The [MemoryAddon] applies the logic of creating, storing, and removing [memories][Memory]. */
abstract class MemoryAddon<M : Memory> : BotAddon {

    /** The [logger] for this [BotAddon]. Set to off by default. */
    protected val logger: Logger = Logger().apply { level = LogLevel.OFF }

    /** The memory provider which will produce memories for this [MemoryAddon]. */
    abstract val provider: MemoryProvider<M>

    override val name: String = ADDON_NAME

    /** Get an immutable version of all Memories. */
    abstract val memories: Map<Long, M>

    /** Get the [memory][M] at the given [key]. */
    abstract suspend fun getMemory(key: Long): M?

    /** Add a new [memory] at the given [key]. Returns the passed [memory]. */
    abstract suspend fun remember(key: Long, memory: M): M

    /** Add all the [memories][Memory]. */
    open suspend fun rememberAll(memories: Map<Long, M>) = memories.forEach { remember(it.key, it.value) }

    /**
     * Modify a [Memory] using a DSL [scope]. This function can be used to ensure a backing or remote field is updated.
     * Returns the modified memory or `null` if one was not found at the given [key].
     */
    abstract suspend fun modifyMemory(key: Long, scope: suspend M.() -> Unit): M?

    /** Remove the [Memory] at the given [key]. Returns the removed memory or `null` if one was not found. */
    abstract suspend fun forget(key: Long): M?

    companion object {
        /** The name of the memory addon. */
        const val ADDON_NAME: String = "memory"
    }

}

/**
 * Strife's built-in [MemoryAddon] implementation.
 *
 * !NOTE! [StrifeMemoryAddon] is NOT persistent across runs. These memories do not stay between launches of the bot
 * . To add persistence to Strife, you will need to implement your own [MemoryAddon].
 *
 * @param existingMemories Any pre-existing memories.
 * @property provider The memory provider which will produce memories for [Guild]s and [User]s
 */
class StrifeMemoryAddon<M : Memory> internal constructor() : MemoryAddon<M>() {

    /** The backing field containing all memories for this addon. */
    private val backingMemories = mutableMapOf<Long, M>()

    override val memories: Map<Long, M> get() = backingMemories.toMap()

    override val provider: MemoryProvider<M> = MemoryProvider()

    /** Whether a [Memory] should be removed when a [Guild] or [DmChannel] is removed. */
    var removeOnExit: Boolean = true

    override suspend fun getMemory(key: Long): M? = backingMemories[key]

    override suspend fun remember(key: Long, memory: M): M {
        backingMemories[key] = memory
        logger.debug("Created Memory at $key (Manual)")
        return memory
    }

    override suspend fun rememberAll(memories: Map<Long, M>) {
        backingMemories.putAll(memories)
        logger.debug("Created Memories at (Manual) ${memories.keys}")
    }

    override suspend fun modifyMemory(key: Long, scope: suspend M.() -> Unit): M? = getMemory(key)?.also { scope(it) }

    override suspend fun forget(key: Long): M? = backingMemories.remove(key)
        ?.also { logger.debug("Removed Memory at $key (Manual)") }

    override fun installTo(scope: BotBuilder) {
        scope.apply {

            // Turn the logger on if the BotBuilder's logger is on
            if (logToConsole) logger.level = LogLevel.TRACE

            // Add a memory when a new Guild is created (joined)
            provider.guild?.let {
                onEvent<GuildCreateEvent> {
                    if (!backingMemories.containsKey(guild.id)) {
                        backingMemories[guild.id] = it(guild)
                        logger.debug("Created Memory for Guild at ${guild.id} (Automatic)")
                    }
                }
                // Remove a memory when a Guild is removed
                if (removeOnExit) onEvent<GuildDeleteEvent> {
                    backingMemories.remove(guildID)
                        ?.also { logger.debug("Removed Memory for Guild $guildID (Automatic)") }
                }
            }

            provider.user?.let { up ->
                onEvent<ChannelCreateEvent> {
                    (channel as? DmChannel)?.recipient()?.let {
                        if (!backingMemories.containsKey(it.id)) {
                            backingMemories[it.id] = up(it)
                            logger.debug("Created Memory for User at ${it.id} (Automatic)")
                        }
                    }
                }
                if (removeOnExit) {
                    onEvent<ChannelDeleteEvent> {
                        (channel as? DmChannel)?.recipient()?.let { u ->
                            backingMemories.remove(u.id)
                                ?.also { logger.debug("Removed Memory for User at ${u.id} (Automatic)") }
                        }
                    }
                }
            }
        }
    }

    /** Set the [guild] memory provider function. */
    fun guild(maker: suspend (Guild) -> M) = provider.guild(maker)

    /** Set the [user] memory provider function. */
    fun user(maker: suspend (User) -> M) = provider.user(maker)

    companion object {
        /**
         * The [BotAddonProvider] of the [StrifeMemoryAddon].
         *
         * @property removeOnExit See [MemoryAddon.removeOnExit].
         * @property existingMemories Any pre-existing memories to be added to the [StrifeMemoryAddon].
         * @property memoryProvider The MemoryProvider for the [provided][provide] [StrifeMemoryAddon].
         */
        operator fun <T : Memory> invoke(): BotAddonProvider<StrifeMemoryAddon<T>> =
            object : BotAddonProvider<StrifeMemoryAddon<T>> {
                override fun provide(): StrifeMemoryAddon<T> = StrifeMemoryAddon<T>()
            }
    }

}
