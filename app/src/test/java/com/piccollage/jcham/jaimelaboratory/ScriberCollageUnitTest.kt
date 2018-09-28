package com.piccollage.jcham.jaimelaboratory

import org.junit.Test
import org.junit.Assert.*

import org.assertj.core.api.Assertions.*

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


open class Scrap(val id: String, val center: Point): IScribeable {
    override fun scribe(s: IScribeWriter) {
        s.write("id", id)
        s.write("center", center)
    }
}
fun unscribeScrap(s: IScribeReader): Scrap? =
    when (s.read_String("type")) {
        "image" -> unscribeImageScrap(s)
        "text" -> unscribeTextScrap(s)
        else -> null
    }

class ImageScrap(id: String, center: Point, val imageUrl: String): Scrap(id, center) {
    override fun scribe(s: IScribeWriter) {
        super.scribe(s)
        s.write("type", "image")
        s.write("imageUrl", imageUrl)
    }
}
fun unscribeImageScrap(s: IScribeReader): ImageScrap? =
    ImageScrap(
        s.read_String("id")!!,
        s.read("center", ::unscribePoint) as Point,
        s.read_String("imageUrl")!!
    )

class TextScrap(id: String, center: Point, val text: String): Scrap(id, center) {
    override fun scribe(s: IScribeWriter) {
        super.scribe(s)
        s.write("type", "text")
        s.write("text", text)
    }
}
fun unscribeTextScrap(s: IScribeReader): TextScrap? =
    TextScrap(
        s.read_String("id")!!,
        s.read("center", ::unscribePoint) as Point,
        s.read_String("text")!!
    )

class Collage(val size: Point, val scraps: List<Scrap> = listOf()): IScribeable {
    var backgroundScrap: Scrap? = null
    override fun scribe(s: IScribeWriter) {
        s.write("size", size)
        s.write("scraps", scraps)
        backgroundScrap?.let { s.write("backgroundScrap", it) }
    }
}
fun unscribeCollage(s: IScribeReader): Collage? =
    Collage(
        s.read("size", ::unscribePoint) as Point,
        s.read_List("scraps", ::unscribeScrap) as List<Scrap>
    ).apply {
        backgroundScrap = s.read("backgroundScrap", ::unscribeScrap) as Scrap
    }

class ScriberCollageUnitTest {

    @Test
    fun `basic functionality`() {
        val c = Collage(Point(200f, 300f),
                    listOf<Scrap>(
                     TextScrap("t1", Point(100f, 120f), "Hello!"),
                     ImageScrap("i1", Point(110f, 190f), "image1.jpg"),
                     ImageScrap("i2", Point(250f, 220f), "image2.jpg")
                    )
                )
        c.backgroundScrap = c.scraps[1]
        val json = JsonScribeWriter().apply {
                       /* ScriberReferenceWriter(this). */ write("the collage", c)
                   }.result
        val jsonS = mapOf(
            "the collage" to mapOf(
                "size" to mapOf("x" to 200.0f, "y" to 300.0f),
                "scraps" to listOf(
                    mapOf(
                        "id" to "t1", "type" to "text",
                        "center" to mapOf("x" to 100.0f, "y" to 120.0f),
                        "text" to "Hello!"
                    ),
                    mapOf(
                        "id" to "i1", "type" to "image",
                        "center" to mapOf("x" to 110.0f, "y" to 190.0f),
                        "imageUrl" to "image1.jpg"
                    ),
                    mapOf(
                        "id" to "i2", "type" to "image",
                        "center" to mapOf("x" to 250.0f, "y" to 220.0f),
                        "imageUrl" to "image2.jpg"
                    )
                ),
                // This COPIES the Scrap definition (need to enable reference checking)
                "backgroundScrap" to mapOf(
                        "id" to "i1", "type" to "image",
                        "center" to mapOf("x" to 110.0f, "y" to 190.0f),
                        "imageUrl" to "image1.jpg"
                )
            )
        )
        assertThat(json).isEqualToComparingFieldByFieldRecursively(jsonS)

        val collageS = JsonScribeReader(json).read("the collage", ::unscribeCollage)
        assertThat(collageS).isEqualToComparingFieldByFieldRecursively(c)

    }
}
