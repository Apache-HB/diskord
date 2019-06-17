package cache

//import com.ampro.kpack.test.Benchmarker

class LruWeakCacheBench /*: Benchmarker<LruWeakCache<Int, Int>>({
    generate = { LruWeakCache(maxSize = 1_000, minSize = 100, trashSize = 10, load = { -it }) }

    "fill" { c -> (0 until c.maxSize).forEach { c.put(it, it) } }

    "overfill"(pre = { "fill"(it) }) { c -> c.put(c.maxSize + 10, c.maxSize + 10) }

    "get trashed value"(pre = { "overfill"(it) }) { c -> c[0] }
}) {
    @Test override fun run() = super.run()
}*/
