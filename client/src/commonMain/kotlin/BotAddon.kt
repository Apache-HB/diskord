package com.serebit.strife

/**
 * A BotAddon is a means by which to extend the behavior of a [BotClient] in a module-agnostic way. A addon must
 * provide a [name] by which it can be referenced, along with the [routine][installTo] it should use to install itself
 * to its [BotBuilder].
 */
interface BotAddon {
    /** The unique name of this [addon][BotAddon]. */
    val name: String

    /** Installs this [addon][BotAddon] to the given [BotBuilder] [scope]. */
    fun installTo(scope: BotBuilder)
}

/** Provides a [BotAddon] with type [TAddon]. */
interface BotAddonProvider<TAddon : BotAddon> {
    /** Returns an instance of [TAddon]. */
    fun provide(): TAddon
}
