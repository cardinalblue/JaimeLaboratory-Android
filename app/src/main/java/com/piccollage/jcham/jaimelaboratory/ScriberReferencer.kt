package com.piccollage.jcham.jaimelaboratory

interface IScribeReferenceable: IScribeable {
    val reference: String?
}

class ScriberReferencerWriter(
        private val inner: IScribeWriter,
        private val cache: MutableSet<IScribeReferenceable> = HashSet<IScribeReferenceable>()
        ): IScribeWriter by inner {

    private fun wrapScriber(outer: Scriber): Scriber {
        return fun(inner: IScribeWriter, scribeable: IScribeable?): Unit? {
            return outer(ScriberReferencerWriter(inner, cache), scribeable)
        }
     }

    override fun write(key: String, value: IScribeable?, scriber: Scriber) {

        // See if referenceable
        (value as? IScribeReferenceable)?.let { referenceable ->
            // See if in cache
            if (cache.contains(referenceable)) {
                // See if it has the reference
                referenceable.reference?.let { reference ->
                    // Write the reference
                    inner.write(key, MapScribeable(mapOf("\$ref" to reference)), scriber)
                    return
                }
            }
            else {
                cache.add(referenceable)
            }
        }
        // else, normal handling
        inner.write(key, value, wrapScriber(scriber))
    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        inner.write(key, value, wrapScriber(scriber))
    }

}

class ScriberReferencerReader(
    private val inner: IScribeReader,
    private val cache: MutableMap<String, IScribeReferenceable> = mutableMapOf()
): IScribeReader by inner {

    private fun wrapUnscriber(outer: Unscriber): Unscriber {
        return fun(reader: IScribeReader): IScribeable? {
            // See if has a reference
            reader.readString("\$ref")?.iff { reference ->
                // See if in cache
                cache[reference]?.iff { return it }
            }
            // Otherwise, normal handling
            return outer(ScriberReferencerReader(reader, cache)).also { scribeable ->
                // If referenceable, save it to cache
                (scribeable as? IScribeReferenceable)?.iff {
                    scribeable.reference?.iff { reference ->
                        cache[reference] = scribeable
                    }
                }
            }
        }
    }

    override fun read(key: String, unscriber: Unscriber): IScribeable? {
        return inner.read(key, wrapUnscriber(unscriber))
    }
    override fun readList(key: String, unscriber: Unscriber): List<Any?>? {
        return inner.readList(key, wrapUnscriber(unscriber))
    }

}