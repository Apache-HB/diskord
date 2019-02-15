package cache

import com.serebit.strife.internal.LRUCache
import kotlin.test.*

/**
 * Tests a [LRUCache] with additional tests for removing the
 * [evict target][LRUCache.evictTarget] on [max size][LRUCache.maxSize] reached.
 *
 * @author JonoAugustine (HQRegent)
 */
class LRUCacheTest : CacheTest<Int, String> {
    lateinit var cache: LRUCache<Int, String>

    @BeforeTest
    override fun `build cache`() {
        cache = LRUCache(1, 10)
    }

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
    override fun `set from map`() {
        val map = mutableMapOf<Int, String>()
        TEST_KEYS.forEach { i ->
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
            assertTrue(i in cache, "$i -> ${cache[i]}")
            cache.minusAssign(i)
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
        assertTrue(cache.containsValue(TEST_STRING * it), "$it -> ${cache[it]}")
    }

    @Test
    override fun `set and hasPair`() = TEST_KEYS.forEach {
        cache[it] = TEST_STRING * it
        assertEquals(cache[it], TEST_STRING * it)
    }

    companion object {
        const val TEST_STRING = "X"
        val TEST_KEYS = List(10) { it + 1 }
    }
}

private operator fun String.times(n: Int) = this.repeat(n)