package com.serebit.strife

/** A BotFeature is a means by which to extend the behavior of a [Context] in a module-agnostic way. A feature must
 * provide a [name] by which it can be referenced, along with the [routine][installTo] it should use to install itself
 * to its [BotBuilder].
 */
interface BotFeature {
    val name: String

    fun installTo(scope: BotBuilder)
}
