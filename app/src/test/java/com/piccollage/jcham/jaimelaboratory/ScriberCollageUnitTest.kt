package com.piccollage.jcham.jaimelaboratory

import org.junit.Test
import org.assertj.core.api.Assertions.*
import org.w3c.dom.Text

// ================================================================
// Collage/Scrap tests

data class Point(val x: Float, val y: Float): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("x", x)
        s.write("y", y)
    }
    constructor(s: IScribeReader): this(s.readFloat("x") ?: 0f,
                                        s.readFloat("y") ?: 0f)
}

open class Scrap(val id: String, val center: Point): IScribeable, IScribeReferenceable {
    override val reference get() = "/scraps/${id}"
    override fun scribe(s: IScribeWriter) {
        s.write("id", id)
        s.write("center", center)
    }
}
fun Scrap(s: IScribeReader): Scrap? =
    when (s.readString("type")) {
        "image" -> ImageScrap(s)
        "text"  -> TextScrap(s)
        else -> null
    }

class ImageScrap(id: String, center: Point, val imageUrl: String): Scrap(id, center) {
    override fun scribe(s: IScribeWriter) {
        super.scribe(s)
        s.write("type", "image")
        s.write("imageUrl", imageUrl)
    }
    constructor(s: IScribeReader): this(
                s.readString("id")!!,
                s.read("center", ::Point) as Point,
                s.readString("imageUrl")!!
                )
}

class TextScrap(id: String, center: Point, val text: String): Scrap(id, center) {
    override fun scribe(s: IScribeWriter) {
        super.scribe(s)
        s.write("type", "text")
        s.write("text", text)
    }
    constructor(s: IScribeReader): this(
                s.readString("id")!!,
                s.read("center", ::Point) as Point,
                s.readString("text")!!
                )
}

class Collage(val size: Point, val scraps: List<Scrap> = listOf()): IScribeable {
    var backgroundScrap: Scrap? = null
    override fun scribe(s: IScribeWriter) {
        s.write("size", size)
        s.write("scraps", scraps)
        backgroundScrap?.let {
            s.write("backgroundScrap", it)
        }
    }
    constructor(s: IScribeReader): this(
                s.read("size", ::Point) as Point,
                s.readList("scraps", ::Scrap) as List<Scrap>
                ) {
        backgroundScrap = s.read("backgroundScrap", ::Scrap) as? Scrap
    }
}

class ScriberCollageUnitTest {

    @Test
    fun `basic functionality`() {

        // ---- Collage setup
        val c = Collage(Point(200f, 300f),
                listOf<Scrap>(
                        TextScrap("t1", Point(100f, 120f), "Hello!"),
                        ImageScrap("i1", Point(110f, 190f), "image1.jpg"),
                        ImageScrap("i2", Point(250f, 220f), "image2.jpg")
                )
        )
        c.backgroundScrap = c.scraps[1]

        // ---- This is what the JSON output should look like
        fun <K, V> M(vararg pairs: Pair<K, V>): Map<K, V> = mutableMapOf<K, V>(*pairs)
        val jsonS = M(
            "the collage" to M(
                "size" to M("x" to 200.0f, "y" to 300.0f),
                "scraps" to listOf(
                    M(
                        "id" to "t1", "type" to "text",
                        "center" to M("x" to 100.0f, "y" to 120.0f),
                        "text" to "Hello!"
                    ),
                    M(
                        "id" to "i1", "type" to "image",
                        "center" to M("x" to 110.0f, "y" to 190.0f),
                        "imageUrl" to "image1.jpg"
                    ),
                    M(
                        "id" to "i2", "type" to "image",
                        "center" to M("x" to 250.0f, "y" to 220.0f),
                        "imageUrl" to "image2.jpg"
                    )
                ),
                // This COPIES the Scrap definition (need to enable reference checking)
                "backgroundScrap" to M(
                    "id" to "i1", "type" to "image",
                    "center" to M("x" to 110.0f, "y" to 190.0f),
                    "imageUrl" to "image1.jpg"
                )
            )
        )

        // ---- First write without references
        val writerJson = JsonScribeWriter()
        var json = writerJson.apply { write("the collage", c) }.result

        // ---- See correct JSON was generated
        assertThat(json).isEqualToComparingFieldByFieldRecursively(jsonS)

        // ---- Read from JSON what we wrote and compare to original
        val collageS1 = JsonScribeReader(json).read("the collage", ::Collage)
        assertThat(collageS1).isEqualToComparingFieldByFieldRecursively(c)
        println("Read collage ${collageS1}")

        // ================================================================

        // ---- Enable references and write again
        val writerRefs = ScriberReferencerWriter(writerJson).apply { write("the collage", c) }
        json = writerJson.apply { write("the collage", c) }.result

        // ---- Modify the generated JSON to have a reference instead
        val jsonCollage = jsonS.get("the collage") as MutableMap<String, Any?>
        jsonCollage.set("backgroundScrap", M("\$ref" to "/scraps/i1"))
        println("Comparison collage with reference ${jsonCollage}")

        // ---- Write JSON again, should have reference now
        ScriberReferencerWriter(writerJson).apply { write("the collage", c) }
        json = writerJson.result
        assertThat(json).isEqualToComparingFieldByFieldRecursively(jsonS)
        println("Compared new JSON is ok!!")

        // ---- Read from JSON what we wrote and compare to original
        val collageS2 = ScriberReferencerReader(JsonScribeReader(json)).read("the collage", ::Collage)
        println("Read collage ${collageS2} from json")
        assertThat(collageS2).isEqualToComparingFieldByFieldRecursively(c)
    }
}
