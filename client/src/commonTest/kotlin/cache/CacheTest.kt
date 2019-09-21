package cache

import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * An interface for making tests for StrifeCache implementations
 *
 * @author JonoAugustine (HQRegent)
 */
interface CacheTest<K, V> {
    @BeforeTest
    fun `build cache`()

    /** Set [key][K]-[value][V] then get same key */
    @Test
    fun `set and get same`()

    /** Attempt get from empty */
    @Test
    fun `get on empty return null`()

    /** Set [key][K]-[value][V] then get different `(should return null)` */
    @Test
    fun `set and get wrong`()

    /** Set [key][K]-[value][V] from [Pair] then get same entry */
    @Test
    fun `set from pair`()

    /** Set multiple [key][K]-[value][V] pairs then get same entries. */
    @Test
    fun `set from map`()

    @Test
    fun `set and remove`()

    @Test
    fun size()

    @Test
    fun clear()

    /** set a [key][K]-[value][V] and confirm contains [key][K]. */
    @Test
    fun `set and hasKey`()

    /** set a [key][K]-[value][V] and confirm contains [key][K]-[value][V]. */
    @Test
    fun `set and hasPair`()
}
