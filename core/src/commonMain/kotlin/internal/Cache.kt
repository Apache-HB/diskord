package com.serebit.strife.internal

/**
 * A Caching Interface which presents a framework for abstracting away from a [Map],
 * allowing for more detailed internal control over caching behavior.
 */
internal abstract class StrifeCache<K, V> {
    data class CacheEntry<K, V>(val key: K, val value: V)
    /** Internal map of the cache */
    protected abstract val map: MutableMap<K, V>
    open val size: Int get() = map.size
    /** An immutable list of the cache's keys. */
    open val entries get() = map.entries.map { CacheEntry(it.key, it.value) }
    /** An immutable list of the cache's keys. */
    open val keys get() = map.keys.toList()
    /** An immutable list of the cache's values. */
    open val values get() = map.values.toList()
    /** An immutable clone of the cache's current state. */
    open val image get() = map.toMap()
    /** Get the value associated with the [key] from cache. */
    abstract suspend fun get(key: K): V?
    /** Add a [key]-[value] pair to the cache. Returns the previous value associated with the [key]. */
    abstract fun put(key: K, value: V): V?
    /** Remove a [key]-[value][V] entry from cache. Returns the removed [value][V]. */
    abstract fun remove(key: K): V?
    open operator fun contains(key: K) = map.containsKey(key)
    open operator fun set(k: K, v: V) { put(k, v) }
    /** Remove a [key]-[value][V] entry from cache. */
    open operator fun minusAssign(key: K) { this.remove(key) }
    open operator fun plusAssign(entry: Pair<K, V>) { this[entry.first] = entry.second }
    open fun putAll(from: Map<out K, V>) = from.forEach { (k, v) -> this[k] = v }
    open fun containsValue(value: V) = map.containsValue(value)
    open fun containsKey(key: K) = map.containsKey(key)
    open fun isEmpty() = map.isEmpty()
    open fun clear() = map.clear()
}

/** A [StrifeCache] implementation which prioritizes the usage time of entries during size maintenance. */
internal abstract class UsagePriorityCache<K, V> : StrifeCache<K, V>() {
    /** An internal list used to track the usage of entries. */
    protected open val usageRanks = UsageList()
    /** The entry to remove when the list has reached capacity and needs to insert a new value */
    protected abstract val evictTarget: K?

    /** A Doubly Linked List implementation that allows for instant access to a node through a [HashMap]. */
    protected inner class UsageList {
        inner class Node(var next: Node? = null, var prev: Node? = null, var key: K? = null) {
            init {
                key?.also { hashmap[it] = this }
            }
        }

        private val hashmap = HashMap<K, Node>()
        private val head = Node()
        private val tail = Node(prev = head).also { head.next = it }
        var size = 0
        /** Get the first [K] in the list */
        val first: K? get() = head.next?.key
        /** Get the last [K] in the list */
        val last: K? get() = tail.prev?.key

        /** Add [key] to the front of the list. Will move the [key] if it already exists. */
        fun addFront(key: K) {
            val n = hashmap[key]?.also {
                // If key exists, disconnect it
                // Connect prev to n.next. n.prev is never a head/tail
                it.prev!!.next = it.next
                // Connect next to prev
                it.next!!.prev = it.prev
            } ?: Node(key = key)

            // Connect to head & head.next
            n.next = head.next
            n.prev = head
            head.next!!.prev = n
            head.next = n
            size++
        }

        fun removeLast(): K? {
            if (size == 0) return null
            // ... <-> [pp] <-> [p] <-> [t]
            // ... <-> [pp] <-> [t]
            val k = tail.prev!!.key!!.also(hashmap::minusAssign)
            tail.prev!!.prev!!.next = tail
            tail.prev = tail.prev!!.prev
            size--
            return k
        }

        fun removeFirst(): K? {
            if (size == 0) return null
            // [h] <-> [n] <-> [nn] ...
            // [h] <-> [nn] ...
            val k = head.next!!.key!!.also(hashmap::minusAssign)
            head.next!!.next!!.prev = head
            head.next = head.next!!.next
            size--
            return k
        }

        operator fun minusAssign(key: K) { remove(key) }

        fun remove(key: K): K? = hashmap.remove(key)?.also { node ->
            node.prev!!.next = node.next
            node.next!!.prev = node.prev
            size--
        }?.key

        fun clear() {
            hashmap.clear()
            head.next = tail
            tail.prev = head
            size = 0
        }
    }

    companion object {
        const val DEFAULT_MIN = 100
        const val DEFAULT_MAX = 10_000
    }
}

/**
 * An Implementation of [UsagePriorityCache] which removes [trashSize]-number of the least recently used entry
 * when space is needed. Stores by [key][K]/[value][V] pairs, and takes the [minimum size][minSize] and
 * [maximum size][maxSize] of the cache as constructor parameters.
 *
 * See [LRU](https://en.wikipedia.org/wiki/Cache_replacement_policies#LRU)
 *
 * @property minSize The minimum size the [LruCache] will self reduce to during downsizing.
 * *This takes priority over [trashSize]*.
 * @property maxSize the maximum number of entries allowed before new entries will cause downsizing.
 * @property trashSize The number of elements to remove during a downsizing.
 * @property refresh An optional function to attempt to refresh an entry when it was not found in cache.
 */
internal class LruCache<K, V>(
    val maxSize: Int = DEFAULT_MAX,
    val minSize: Int = DEFAULT_MIN,
    val trashSize: Int = DEFAULT_TRASH_SIZE,
    val refresh: suspend (K) -> V? = { null }
) : UsagePriorityCache<K, V>() {
    override val map = mutableMapOf<K, V>()
    override val evictTarget get() = usageRanks.removeLast()

    init {
        if (trashSize < 1) throw IllegalArgumentException("LRU TrashSize must be greater than 0.")
    }

    /**
     * Set a [Key][K]-[Value][V] pair in cache. If the cache is at [maxSize],
     * remove [trashSize]-number [entries][evictTarget] then add the new entry.
     * @return the [value][V] previously at [key]
     */
    override fun put(key: K, value: V): V? {
        // Downsize on max-size
        if (!containsKey(key) && size == maxSize) {
            var i = 1
            while (size > minSize && i++ < trashSize)
                evictTarget?.let(this::remove) ?: break // break if empty
        }
        return map.put(key.apply(usageRanks::addFront), value)
    }

    /** Returns the [value][V] associated with the [key] and sets it to most recently used. */
    override suspend fun get(key: K): V? = map[key]?.also { usageRanks.addFront(key) }
        ?: refresh(key)?.also { put(key, it) }

    override fun clear() {
        map.clear()
        usageRanks.clear()
    }

    override fun remove(key: K): V? = map.remove(key)?.also { usageRanks -= key }

    companion object {
        const val DEFAULT_TRASH_SIZE = 1
    }
}

internal expect class WeakReference<T : Any>(reference: T) {
    fun get(): T?
}

/**
 * A class that provides the ability to weaken and strengthen a reference on demand.
 * The reference will be strong by default.
 */
internal class WeakableReference<T : Any>(reference: T) {
    /** This variable stores the strong reference, or `null` if the reference is currently weak. */
    private var strongReference: T? = reference
    /** This variable stores the weak reference, or `null` if the reference is currently strong. */
    private var weakReference: WeakReference<T>? = null

    /** Returns the reference, or `null` if the reference is no longer available. */
    fun get(): T? = strongReference ?: weakReference?.get()

    /** Strengthens and returns the reference, or `null` if the reference is no longer available. */
    fun strengthen(): T? = weakReference?.get()?.also {
        strongReference = it
        weakReference = null
    } ?: strongReference

    /** Weakens and returns the reference, or `null` if the reference is no longer available. */
    fun weaken(): T? = strongReference?.also {
        weakReference = WeakReference(it)
        strongReference = null
    } ?: weakReference?.get()
}
