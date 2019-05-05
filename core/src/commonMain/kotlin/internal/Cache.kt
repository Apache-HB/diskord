package com.serebit.strife.internal

/** A wrapper object which holds only a weak reference to the contained object. */
internal expect class WeakReference<T : Any>(reference: T) {
    fun get(): T?
}

/** A Doubly Linked List implementation that allows for instant access to a node through a [HashMap]. */
private class UsageList<K> {
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

    fun remove(key: K): K? = hashmap.remove(key)?.also { node ->
        node.prev!!.next = node.next
        node.next!!.prev = node.prev
        size--
    }?.key

    operator fun minusAssign(key: K) {
        remove(key)
    }

    fun clear() {
        hashmap.clear()
        head.next = tail
        tail.prev = head
        size = 0
    }

    override fun toString(): String {
        val list = mutableListOf<K?>()
        var n = head.next!!
        while (n != tail) {
            list.add(n.key)
            n = n.next!!
        }
        return list.toString()
    }
}

/**
 * TODO Docs including [WeakReference]
 * A caching implementation which removes [trashSize]-number of the least recently used entry
 * when space is needed. Stores by [key][K]/[value][V] pairs, and takes the [minimum size][minSize] and
 * [maximum size][maxSize] of the cache as constructor parameters.
 *
 * See [LRU](https://en.wikipedia.org/wiki/Cache_replacement_policies#LRU)
 *
 * @property minSize The minimum size the [LruCache] will self reduce to during downsizing.
 * *This takes priority over [trashSize]*.
 * @property maxSize the maximum number of entries allowed before new entries will cause downsizing.
 * @property trashSize The number of elements to remove during a downsizing.
 * @property load An optional function to attempt to load an entry when it was not found in cache.
 */
class LruWeakCache<K, V : Any>(
    val maxSize: Int = DEFAULT_MAX,
    val minSize: Int = DEFAULT_MIN,
    val trashSize: Int = DEFAULT_TRASH_SIZE,
    val load: /*todo suspend*/ (K) -> V? = { null }
) : Iterable<LruWeakCache<K, V>.CacheEntry> {

    inner class CacheEntry internal constructor(val key: K, val value: V)

    /** Internal map of LRU cache entries. */
    private val liveMap = mutableMapOf<K, V>()
    /** Internal map of weak reference entries. */
    private val weakMap = mutableMapOf<K, WeakReference<V>>()

    /** An internal list used to track the usage of entries. */
    private val usageRanks = UsageList<K>()
    /** The entry to remove when the list has reached capacity and needs to insert a new value. */
    private val evictTarget get() = usageRanks.removeLast()

    val size: Int get() = liveMap.size
    /** An immutable clone of the cache's current state excluding weak entries. */
    val image get() = liveMap.toMap()
    /** An immutable list of the cache's keys. */
    val keys get() = liveMap.keys.toList()
    /** An immutable list of the cache's values. */
    val values get() = liveMap.values.toList()

    init {
        if (trashSize < 1) throw IllegalArgumentException("LRU TrashSize must be greater than 0.")
    }

    private fun weaken(key: K, value: V): WeakReference<V> = WeakReference(value).also { weak ->
        if (weakMap.size >= maxSize) {
            val clean = weakMap.filter { it.value.get() != null }
            weakMap.clear()
            weakMap.putAll(clean)
        }
        weakMap[key] = weak
    }

    /**
     * Set a [Key][K]-[Value][V] pair in cache. If the cache is at [maxSize],
     * weaken [trashSize]-number [entries][evictTarget] then add the new entry.
     *
     * @return the [value][V] previously at [key]
     */
    fun put(key: K, value: V): V? {
        var old: V? = null
        // If the key is mapped to a weak value
        if (weakMap.containsKey(key)) old = weakMap.remove(key)?.get()
        // Downsize on max-size
        if (size >= maxSize) {
            var i = 1
            while (size > minSize && i++ < trashSize) {
                // evict target to weakMap
                evictTarget?.also { k -> remove(k)?.also { v -> weaken(k, v) } } ?: break // break on empty
            }
        }
        return liveMap.put(key.apply(usageRanks::addFront), value) ?: old
    }

    /** Returns the [value][V] associated with the [key] and sets it to most recently used. */
    operator fun get(key: K): V? = liveMap[key]?.also { usageRanks.addFront(key) }
        ?: weakMap.remove(key)?.get()?.also { v -> put(key, v) }
        ?: load(key)?.also { put(key, it) }

    /** Remove a [key]-[value][V] entry from cache. Returns the removed [value][V]. */
    fun remove(key: K): V? = (liveMap.remove(key) ?: weakMap.remove(key)?.get())?.also { usageRanks.remove(key) }

    operator fun contains(key: K) = liveMap.containsKey(key) ||
            weakMap[key]?.let { if (it.get() == null) weakMap.remove(key).let { false } else true } ?: false

    fun isEmpty() = liveMap.isEmpty() && weakMap.isEmpty()

    fun clear() {
        liveMap.clear()
        weakMap.clear()
        usageRanks.clear()
    }

    override fun iterator(): Iterator<CacheEntry> = (liveMap.map { CacheEntry(it.key, it.value) } +
            weakMap.mapNotNull { (k, weakV) ->
                weakV.get()?.let { v -> CacheEntry(k, v) } ?: null.also { weakMap.remove(k) }
            }).iterator()

    companion object {
        const val DEFAULT_MIN = 100
        const val DEFAULT_MAX = 10_000
        const val DEFAULT_TRASH_SIZE = 1
    }
}

/** Remove a [key]-[value][V] entry from cache. */
operator fun <K, V : Any> LruWeakCache<K, V>.minusAssign(key: K) {
    remove(key)
}

/**
 * Set a [Key][K]-[Value][V] pair in cache. If the cache is at [maxSize],
 * weaken [trashSize]-number [entries][evictTarget] then add the new entry.
 *
 * @return the [value][V] previously at [key]
 */
operator fun <K, V : Any> LruWeakCache<K, V>.set(k: K, v: V) {
    put(k, v)
}

/**
 * Set a [Key][K]-[Value][V] pair in cache. If the cache is at [LruWeakCache.maxSize],
 * weaken [LruWeakCache.trashSize]-number entries then add the new entry.
 *
 * @return the [value][V] previously at [key]
 */
operator fun <K, V : Any> LruWeakCache<K, V>.plusAssign(entry: Pair<K, V>) {
    set(entry.first, entry.second)
}

fun <K, V : Any> LruWeakCache<K, V>.putAll(from: Map<out K, V>) = from.forEach { (k, v) -> set(k, v) }
