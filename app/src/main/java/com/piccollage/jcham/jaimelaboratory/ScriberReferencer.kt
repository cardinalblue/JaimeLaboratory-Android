package com.piccollage.jcham.jaimelaboratory

class ScriberReferenceWriter(
        val inner: IScribeWriter,
        val cache: MutableSet<IScribeReferenceable> = HashSet<IScribeReferenceable>()
        ): IScribeWriter by inner {

    private fun wrapScriber(outer: Scriber): Scriber {
        return { inner: IScribeWriter, s: IScribeable?  ->
            outer(ScriberReferenceWriter(inner, cache), s)
        }
    }

    override fun write(key: String, value: IScribeable?, scriber: Scriber) {
        println("ScribeReferencer ${value}")

        // See if referenceable
        (value as? IScribeReferenceable)?.let { referenceable ->
            // See if in cache
            if (cache.contains(referenceable)) {
                // See if it has the reference
                referenceable.reference?.let { reference ->
                    // Write the reference
                    inner.write(key, mapOf("\$ref" to reference))
                    return
                }
            }
            else {
                println("ScriberReferencer adding ${referenceable}")
                cache.add(referenceable)
            }
        }
        // else, normal handling
        inner.write(key, value, wrapScriber(scriber))
    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        inner.write(key, value, wrapScriber(scriber))
    }
    override fun write(key: String, value: Map<String, Any?>?, scriber: Scriber) {
        inner.write(key, value, wrapScriber(scriber))
    }
}
