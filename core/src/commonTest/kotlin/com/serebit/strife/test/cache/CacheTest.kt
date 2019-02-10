package com.serebit.strife.test.cache

import AxiomSet
import com.serebit.strife.internal.StrifeCache
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * An interface for making tests for [StrifeCache] implementations
 *
 * @author JonoAugustine (HQRegent)
 */
interface CacheTest<K, V> {

    companion object {
        const val TEST_STRING = "X"
        val TEST_KEYS = List(10) { it + 1 }
        val AXIOM_BASE: AxiomSet = AxiomSet("Cache Axioms") {
            "GET"("return value at given key or null if no value exists") {
                example("get(K) -> V?")
                "GET MULTI"("return list of values or an empty list") {
                    example("get(K0...Ki) -> List<V?>")
                }
            }
            "SET"("set Key-Value, return value previously at key") {
                example("set(K, V) -> V?")
                "SET FROM PAIR"("SET(K, V)") {
                    example("plus(pair<K, V>)")
                }
                "SET MULTI"("set multiple Keys to single value") {
                    example("set(K0...Ki, V)")
                }
                "SET FROM MAP"("FOR K,V in map, SET(K, V)") {
                    example("putAll(map)")
                }
            }
            "REMOVE"("remove key-value pair, return removed value") {
                example("minus(key: K): V?")
                "REMOVE ALL"("remove all keys-values, return this") {
                    example("clear() -> StrifeCache<K, V>")
                }
            }
            "CONTAINS"("true if the cache contains the given parameter") {
                "CONTAINS KEY"("contains(K) -> Boolean")
                "CONTAINS VALUE"("has(V) -> Boolean")
                "CONTAINS PAIR"("hasEntry(K, V) -> Boolean")
            }
        }
    }

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

    /** set a [key][K]-[value][V] and confirm contains [value][V]. */
    @Test
    fun `set and hasValue`()

    /** set a [key][K]-[value][V] and confirm contains [key][K]-[value][V]. */
    @Test
    fun `set and hasPair`()
}
