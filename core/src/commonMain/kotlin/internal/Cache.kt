package com.serebit.strife.internal

import com.serebit.strife.internal.StrifeCache.Companion.DEFAULT_MAX
import com.serebit.strife.internal.StrifeCache.Companion.DEFAULT_MIN

/**
 * A Caching Interface which presents a framework for abstracting away from a
 * [Map], allowing for more detailed internal control over caching behavior.
 *
 * @param K the key type
 * @param V the value type
 *
 * @author JonoAugustine (HQRegent)
 * @since 0.0.0
 */
interface StrifeCache<K, V> {

    companion object {
        /** 100 */
        const val DEFAULT_MIN = 100
        /** 1,000 */
        const val DEFAULT_MAX = 10_000
    }

    /** The maximum size of the Cache. */
    val maxSize: Int
    /** The minimum size the cache can self-reduce to. */
    val minSize: Int
    /** The current number of [Key][K]-[Value][V] pairs in the cache. */
    var size: Int

    /**
     * Set a [Key][K]-[Value][V] pair in cache.
     * @return the [value][V] previously at [key]
     */
    operator fun set(key: K, value: V): V?
    /** Set multiple [Keys][K] to a single [Value][V]. */
    operator fun set(vararg keys: K, value: V)

    /** Add all entries from the [map]. */
    fun putAll(map: Map<K, V>)

    /** Get a [Value][V]. */
    operator fun get(key: K): V?
    /**
     * Get multiple [Values][V]
     * @return A [List] of [values][V] with corresponding indexes to the [keys].
     * *The [List] is empty if all values are* `null`
     */
    operator fun get(vararg keys: K): List<V?>

    /** Add a [Key][K]-[Value][V] pair. Same as [StrifeCache.set]. */
    operator fun plus(entry: Pair<K, V>)
    /** Remove a [Key][K]-[Value][V] pair. */
    operator fun minus(key: K): V?

    operator fun contains(key: K): Boolean
    fun hasValue(value: V): Boolean
    fun hasEntry(key: K, value: V): Boolean

    /**
     * Clear the Cache of all keys & values
     * @return this Cache, cleared of all entries
     */
    fun clear(): StrifeCache<K, V>
}

/**
 * A [StrifeCache] implementation which prioritizes the usage time of entries
 * during size maintenance.
 *
 * @author JonoAugustine (HQRegent)
 */
abstract class UsagePriorityCache<K, V>(
    override val minSize: Int = DEFAULT_MIN,
    override val maxSize: Int = DEFAULT_MAX
) : StrifeCache<K, V> {

    override var size: Int get() = map.size; set(_) {}
    /** The backing map of this cache. */
    protected abstract val map: MutableMap<K, V>
    /** An immutable clone of the cache's current state. */
    val image get() = map.mapValues { it.value }.toMap()
    /** An internal list used to track the usage of entries. */
    protected abstract val usageRanks: MutableList<K>

    /**
     * The entry to remove when the list has reached capacity and needs to
     * insert a new value
     */
    abstract val evictTarget: K

    override fun contains(key: K) = map.contains(key)

    override fun hasValue(value: V) = map.any { it.value == value }

    override fun hasEntry(key: K, value: V) = map[key] == value

    /**
     * Clear the Cache of all keys & values
     * @return this Cache, cleared of all entries
     */
    override fun clear(): StrifeCache<K, V> {
        map.clear()
        return this
    }
}

/**
 * An Implementation of [UsagePriorityCache] which removes the least recently
 * used entry when space is needed.
 *
 * [LRU](https://en.wikipedia.org/wiki/Cache_replacement_policies#LRU)
 *
 * @param K key type
 * @param V the value type
 * @param maxSize The maximum size of the cache
 * @param minSize The minimum size the cache can downsize to automatically
 *
 * @author JonoAugustine (HQRegent)
 *
 */
class LRUCache<K, V>(
    override val minSize: Int = DEFAULT_MIN,
    override val maxSize: Int = DEFAULT_MAX
) : UsagePriorityCache<K, V>(minSize, maxSize) {

    override val map: MutableMap<K, V> = mutableMapOf()
    /** 0 == greatest usage or most recent */
    override val usageRanks: MutableList<K> = mutableListOf()
    override val evictTarget: K get() = usageRanks.last()

    /**
     * Set a [Key][K]-[Value][V] pair in cache. If the cache is at [maxSize],
     * remove the [evictTarget] then add the new entry.
     * @return the [value][V] previously at [key]
     */
    override fun set(key: K, value: V): V? {
        if (map.size == maxSize && key != evictTarget) this - evictTarget
        // Don't update usage on set, we only care about get()
        return map.put(key, value)
    }

    override fun set(vararg keys: K, value: V) =
        keys.forEach { this[it] = value }

    override fun plus(entry: Pair<K, V>) { this[entry.first] = entry.second }

    override fun putAll(map: Map<K, V>) =
        map.forEach { this[it.key] = it.value }

    /**
     * Retrieve a [value][V] and set to MOST recently used
     *
     * @return the [value][V] at [key] or null
     */
    override fun get(key: K): V? = map[key]?.also {
        usageRanks.remove(key)
        usageRanks.add(0, key)
    }

    override fun get(vararg keys: K) = List(keys.size) { i -> this[keys[i]] }

    override fun minus(key: K) =
        map.remove(key)?.also { usageRanks.remove(key) }
}
