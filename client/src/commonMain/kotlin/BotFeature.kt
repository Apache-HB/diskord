package com.serebit.strife

/**
 * A BotFeature is a means by which to extend the behavior of a [BotClient] in a module-agnostic way. A feature must
 * provide a [name] by which it can be referenced, along with the [routine][installTo] it should use to install itself
 * to its [BotBuilder].
 */
interface BotFeature {
    /** The unique name of this [feature][BotFeature]. */
    val name: String

    /** Installs this [feature][BotFeature] to the given [BotBuilder] [scope]. */
    fun installTo(scope: BotBuilder)
}

/** Provides a [BotFeature] with type [TFeature]. */
interface BotFeatureProvider<TFeature : BotFeature> {
    /** Returns an instance of [TFeature]. */
    fun provide(): TFeature
}
