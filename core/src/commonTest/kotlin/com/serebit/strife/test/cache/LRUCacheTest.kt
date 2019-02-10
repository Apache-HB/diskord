package com.serebit.strife.test.cache

import AxiomSet.Axiom
import com.serebit.strife.internal.LRUCache
import com.serebit.strife.test.cache.CacheTest.Companion.AXIOM_BASE
import com.serebit.strife.test.cache.CacheTest.Companion.TEST_KEYS
import com.serebit.strife.test.cache.CacheTest.Companion.TEST_STRING
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests a [LRUCache] with additional tests for removing the
 * [evict target][LRUCache.evictTarget] on [max size][LRUCache.maxSize] reached.
 *
 * @author JonoAugustine (HQRegent)
 */
class LRUCacheTest : CacheTest<Int, String> {

    companion object {
        val AXIOM_LRU = AXIOM_BASE.apply {
            axioms[AXIOM_BASE.axioms.indexOfFirst { it.name == "SET" } ]
                .apply {
                    children.add(
                        Axiom(
                            "SET ON MAX_SIZE",
                            "REMOVE evictTarget then SET",
                            this
                        )
                    )
                }
        }.also { println("Testing LRUCache Axioms\n$it") }
    }

    lateinit var cache: LRUCache<Int, String>

    @BeforeTest
    override fun `build cache`() { cache = LRUCache(1, 10) }

    @Test
    override fun `set and get same`() {
        TEST_KEYS.forEach { i ->
            cache[i] = TEST_STRING
            assertEquals(TEST_STRING, cache[i])
            cache[i] = TEST_STRING * i
            cache.image.forEach { e ->
                assertEquals(TEST_STRING * e.key, cache[e.key])
            }
        }
    }

    @Test
    override fun `get on empty return null`() {
        TEST_KEYS.forEach { assertNull(cache[it]) }
    }

    @Test
    override fun `set and get wrong`() {
        cache[0] = TEST_STRING
        assertNotEquals(TEST_STRING, cache[1])
        cache[1] = TEST_STRING * 2
        assertNotEquals(TEST_STRING, cache[1])
    }

    /** Make sure old entries are being removed on max size. */
    @Test
    fun `set on max_size then get`() {
        TEST_KEYS.forEach { cache[it] = TEST_STRING * it }
        TEST_KEYS.forEach { assertEquals(TEST_STRING * it, cache[it]) }

        cache[TEST_KEYS.size] = "NEW"
        assertNull(cache[TEST_KEYS.first()])
        assertEquals("NEW", cache[TEST_KEYS.size])
        assertEquals(1, cache.image.count { it.value == "NEW" })
    }

    @Test
    override fun size() {
        cache.putAll(TEST_KEYS.associate { it to TEST_STRING })
        assertEquals(TEST_KEYS.size, cache.size)
    }

    @Test
    override fun `set from pair`() {
        CacheTest.TEST_KEYS.forEach { i ->
            cache + (i to TEST_STRING)
            assertEquals(TEST_STRING, cache[i])
            cache + (i to TEST_STRING * i)
            cache.image.forEach { e ->
                assertEquals(TEST_STRING * e.key, cache[e.key])
            }
        }
    }

    @Test
    override fun `set from map`() {
        val map = mutableMapOf<Int, String>()
        CacheTest.TEST_KEYS.forEach { i ->
            map[i] = TEST_STRING * i
            cache.putAll(map)
            cache.image.forEach { e ->
                assertEquals(TEST_STRING * e.key, cache[e.key])
            }
        }
    }

    @Test
    override fun `set and remove`() {
        cache.putAll(TEST_KEYS.associate { it to TEST_STRING })
        TEST_KEYS.forEach { i ->
            assertNotNull(cache - i, "$i -> ${cache[i]}")
            assertNull(cache[i], "$i -> ${cache[i]}")
        }
    }

    @Test
    override fun clear() {
        cache.putAll(TEST_KEYS.associate { it to TEST_STRING })
        cache.clear()
        TEST_KEYS.forEach { assertNull(cache[it], "$it -> ${cache[it]}") }
    }

    @Test
    override fun `set and hasKey`() = TEST_KEYS.forEach {
        cache[it] = TEST_STRING
        assertTrue(cache.contains(it), "$it -> null")
        assertTrue(it in cache, "$it -> null")
    }

    @Test
    override fun `set and hasValue`() = TEST_KEYS.forEach {
        cache[it] = TEST_STRING * it
        assertTrue(cache.hasValue(TEST_STRING * it), "$it -> ${cache[it]}")
    }

    @Test
    override fun `set and hasPair`() = TEST_KEYS.forEach {
        cache[it] = TEST_STRING * it
        assertTrue(cache.hasEntry(it, TEST_STRING * it), "$it -> ${cache[it]}")
    }
}

private operator fun String.times(n: Int) = this.repeat(n)
