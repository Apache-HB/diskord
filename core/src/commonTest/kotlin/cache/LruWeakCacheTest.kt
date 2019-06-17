package cache

import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.minusAssign
import com.serebit.strife.internal.putAll
import com.serebit.strife.internal.set
import kotlin.test.*

/**
 * Tests a [LruWeakCache] with additional tests for removing the
 * [evict target][LruWeakCache.evictTarget] on [max size][LruWeakCache.maxSize] reached.
 */
class LruWeakCacheTest : CacheTest<Int, String> {
    private lateinit var cache: LruWeakCache<Int, String>

    @BeforeTest
    override fun `build cache`() {
        cache = LruWeakCache(10, 1, TEST_TRASH) { "R" }
    }

    @Test
    override fun `set and get same`() {
        TEST_KEYS.forEach { i ->
            cache[i] = TEST_STRING
            assertEquals(TEST_STRING, cache[i])
            cache[i] = TEST_STRING.repeat(i)
            cache.image.forEach { e ->
                assertEquals(TEST_STRING.repeat(e.key), cache[e.key])
            }
        }
    }

    @Test
    override fun `get on empty return null`() {
        TEST_KEYS.forEach { assertNull(cache.image[it]) }
    }

    @Test
    override fun `set and get wrong`() {
        cache[0] = TEST_STRING
        assertNotEquals(TEST_STRING, cache[1])
        cache[1] = TEST_STRING.repeat(2)
        assertNotEquals(TEST_STRING, cache[1])
    }

    /** Make sure old entries are being removed on max size. */
    @Test
    fun `set on max_size then get`() {
        TEST_KEYS.forEach { cache[it] = TEST_STRING.repeat(it) }
        TEST_KEYS.forEach { assertEquals(TEST_STRING.repeat(it), cache[it]) }

        cache[TEST_KEYS.size + 1] = "NEW"
        assertEquals("NEW", cache[TEST_KEYS.size + 1])
        val image = cache.image
        assertEquals(1, image.count { it.value == "NEW" })
        for (i in 0 until TEST_TRASH - 1) assertNull(image[i], "$i -> ${image[i]}")
    }

    @Test
    fun `trashed values stay`() {
        // Save the value to be trashed
        val weak = "X"
        cache.put(cache.maxSize + 20, weak)
        // Overfill to trash the value
        (0 until (cache.maxSize * 1.5).toInt()).forEach { cache.put(it, TEST_STRING) }
        // Check that it was reloaded
        assertEquals(weak, cache[cache.maxSize + 20])
    }

    @Test
    fun `dead values are loaded`() {
        // TODO find better way to test this
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
            cache[i] = TEST_STRING.repeat(i)
            cache.image.forEach { e ->
                assertEquals(TEST_STRING.repeat(e.key), cache[e.key])
            }
        }
    }

    @Test
    override fun `set from map`() {
        val map = mutableMapOf<Int, String>()
        TEST_KEYS.forEach { i ->
            map[i] = TEST_STRING.repeat(i)
            cache.putAll(map)
            cache.image.forEach { e ->
                assertEquals(TEST_STRING.repeat(e.key), cache[e.key])
            }
        }
    }

    @Test
    override fun `set and remove`() {
        cache.putAll(TEST_KEYS.associate { it to TEST_STRING })
        TEST_KEYS.forEach { i ->
            assertTrue(i in cache, "$i -> ${cache.image[i]}")
            cache.minusAssign(i)
            assertNull(cache.image[i], "$i -> ${cache.image[i]}")
        }
    }

    @Test
    override fun clear() {
        cache.putAll(TEST_KEYS.associate { it to TEST_STRING })
        cache.clear()
        assertEquals(0, cache.size)
        assertTrue { cache.isEmpty() }
    }

    @Test
    override fun `set and hasKey`() = TEST_KEYS.forEach {
        cache[it] = TEST_STRING
        assertTrue(cache.contains(it), "$it -> null")
        assertTrue(it in cache, "$it -> null")
    }

    @Test
    override fun `set and hasPair`() = TEST_KEYS.forEach {
        cache[it] = TEST_STRING.repeat(it)
        assertEquals(cache[it], TEST_STRING.repeat(it))
    }

    companion object {
        const val TEST_STRING = "X"
        /** 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 */
        val TEST_KEYS = List(10) { it + 1 }
        const val TEST_TRASH = 10
    }
}
