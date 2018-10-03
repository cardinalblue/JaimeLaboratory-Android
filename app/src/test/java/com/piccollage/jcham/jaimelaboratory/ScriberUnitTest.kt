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
        a1?.let { s.write("a1", it) }
        a2?.let { s.write("a2", it) }
        a3?.let { s.write("a3", it) }
        a4?.let { s.write("a4", it) }
        a5?.let { s.write("a5", it) }
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
        val b2: List<Any?>? = null,
        val b3: Map<String, Any?>? = null
): IScribeable {
    override fun scribe(s: IScribeWriter) {
        b1?.let { s.write("b1", it) }
        b2?.let { s.write("b2", it) }
        b3?.let { s.write("b3", it) }
    }
}
fun unscribeB(s: IScribeReader) = B(
        s.read("b1", ::unscribeA) as? A,
        s.read_List("b2", ::unscribeA) as? List<A?>,
        s.read_Map("b3", ::unscribeA) as? Map<String, A?>
)

data class C(
        val c1: B? = null
): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("c1", c1)
    }
}
fun unscribeC(s: IScribeReader) = C(
        s.read("c1", ::unscribeB) as? B
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
                            "a4" to true
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
            mapOf(
                "_4" to A(4000),
                "_5" to A(5000),
                "_6" to "six thousand"
                )
        )
        val json = JsonScribeWriter().apply{ write("bbbb", b) }.result
        assertEquals(
            mapOf(
                "bbbb" to mapOf(
                    "b1" to mapOf("a1" to 1000),
                    "b2" to listOf<Any>(
                        mapOf("a1" to 2000),
                        mapOf("a1" to 3000)
                    ),
                    "b3" to mapOf<String, Any>(
                        "_4" to mapOf("a1" to 4000),
                        "_5" to mapOf("a1" to 5000),
                        "_6" to "six thousand"
                    )
                )
            ),
            json
        )

        // Reverse it!
        val reader = JsonScribeReader(json)
        val reversed = reader.read("bbbb", ::unscribeB)
        assertEquals(b, reversed)
    }

    @Test
    fun `multiple level`() {
        val c = C( B( A(10000) ) )
        val json = JsonScribeWriter().apply{ write("cccc", c) }.result
        assertEquals(
            mapOf(
                "cccc" to mapOf(
                    "c1" to mapOf(
                        "b1" to mapOf(
                            "a1" to 10000
                        )
                    )
                )
            ),
            json
        )

        // Reverse it!
        val reader = JsonScribeReader(json)
        val reversed = reader.read("cccc", ::unscribeC)
        assertEquals(c, reversed)
    }

}

