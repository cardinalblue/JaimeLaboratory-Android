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
    constructor(s: IScribeReader): this(
        s.readInt("a1"),
        s.readFloat("a2"),
        s.readString("a3"),
        s.readBoolean("a4"),
        s.readBoolean("a5")
    )
}

data class B(
        val b1: A? = null,
        val b2: List<Any?>? = null,
        val b3: Map<String, Any?>? = null
): IScribeable {
    override fun scribe(s: IScribeWriter) {
        b1?.let { s.write("b1", it) }
        b2?.let { s.write("b2", it) }
        b3?.let { s.write("b3", MapScribeable(it)) }
    }
    constructor(s: IScribeReader): this(
        s.read("b1", ::A) as? A,
        s.readList("b2", ::A) as? List<A?>,
        (s.read("b3") {
            r: IScribeReader -> MapScribeable(mapOf(
                "_4" to r.read("_4", ::A),
                "_5" to r.read("_5", ::A),
                "_6" to r.readString("_6")
                ))
            } as MapScribeable).map
        )

}

data class C(
        val c1: B? = null
): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("c1", c1)
    }
}
fun unscribeC(s: IScribeReader) = C(
        s.read("c1", ::B) as? B
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
        val reversed = reader.read("a", ::A)
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
        val reversed = reader.read("bbbb", ::B)
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

