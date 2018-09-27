package com.piccollage.jcham.jaimelaboratory

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

data class A(
        val a1: Int?     = null,
        val a2: Float?   = null,
        val a3: String?  = null,
        val a4: Boolean? = null,
        val a5: Boolean? = null
): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("a1", a1)
         .write("a2", a2)
         .write("a3", a3)
         .write("a4", a4)
         .write("a5", a5)
    }
}
fun unscribeA(s: IScribeReader) = A(
    s.read_Int("a1"),
    s.read_Float("a2"),
    s.read_String("a3"),
    s.read_Boolean("a4"),
    s.read_Boolean("a5")
)

data class B(
        val b1: A? = null,
        val b2: List<A?>? = null,
        val b3: Map<String, A?>? = null
): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("b1", b1)
        s.write("b2", b2)
        s.write("b3", b3)
    }
}
fun unscribeB(s: IScribeReader) = B(
        s.read("b1", ::unscribeA) as? A,
        s.read_List("b2", ::unscribeA) as? List<A?>,
        s.read_Map("b3", ::unscribeA) as? Map<String, A?>
)

class ScriberUnitTest {

    @Test
    fun `basic functionality`() {
        val a = A(100, 200.0f, "300", true, null)
        val json = JsonScribeWriter().apply{ write("a", a) }.result
        assertEquals(json,
                     mapOf(
                        "a" to mapOf(
                            "a1" to 100,
                            "a2" to 200.0f,
                            "a3" to "300",
                            "a4" to true,
                            "a5" to null
                        )
                     )
        )

        // Reverse it!
        val reader = JsonScribeReader(json)
        val reversed = reader.read("a", ::unscribeA)
        assertEquals(reversed, a)
    }

    @Test
    fun `two level`() {
        val b = B(
            A(1000),
            listOf(A(2000), A(3000)),
            mapOf("_4" to A(4000), "_5" to A(5000))
        )
        val json = JsonScribeWriter().apply{ write("bbbb", b) }.result
        assertEquals(json,
                mapOf(
                    "b" to mapOf(
                        "b1" to mapOf("a1" to 1000),
                        "b2" to listOf<Any>(
                            mapOf("a1" to 2000), mapOf("a1" to 3000)
                        ),
                        "b3" to mapOf<String, Any>(
                            "_4" to mapOf("a1" to 4000),
                            "_5" to mapOf("a1" to 5000)
                        )
                    )
                )
        )

        // Reverse it!
        val reader = JsonScribeReader(json)
        val reversed = reader.read("bbbb", ::unscribeB)
        assertEquals(reversed, b)
    }

}

// ================================================================
// Collage/Scrap tests

data class Point(val x: Float, val y: Float): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("x", x)
        s.write("y", y)
    }
}
fun unscribePoint(s: IScribeReader): Point? =
        Point(s.read_Float("x") ?: 0f,
              s.read_Float("y") ?: 0f)


class Scrap(val id: String, val center: Point): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("id", id)
        s.write("center", center)
    }
}
class Collage(val size: Point, val scraps: List<Scrap> = listOf()): IScribeable {
    var backgroundScrap: Scrap? = null
    override fun scribe(s: IScribeWriter) {
        s.write("size", size)
        s.write("scraps", scraps)
        s.write("backgroundScrap", backgroundScrap)
    }
}