import com.serebit.strife.internal.move
import kotlin.test.Test
import kotlin.test.assertEquals

class ListExtensionTests {

    val list get() = mutableListOf<Int>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val listFlipped get() = list.reversed()

    @Test
    fun `move single`() {
        val nList = list.move(1, 0)
        assertEquals(1, nList[0])
        assertEquals(0, nList[1])
        nList.move(0, 1)
        assertEquals(1, nList[1])
        assertEquals(0, nList[0])
        nList.move(list.size - 1, 0)
        assertEquals(10, nList[0])
        assertEquals(0, nList[1])
    }

    @Test
    fun `move by mapping`() {
        var nList = list.move { orgIndex, _ -> orgIndex }
        for (i in 0 until nList.size) {
            assertEquals(i, nList[i])
        }
        nList = list.move { _, v -> 10 - v }
        for (i in 0 until listFlipped.size) {
            assertEquals(listFlipped[i], nList[i])
        }
    }

}
