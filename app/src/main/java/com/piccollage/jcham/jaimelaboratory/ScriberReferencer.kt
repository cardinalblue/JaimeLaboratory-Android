package com.piccollage.jcham.jaimelaboratory

interface IScribeReferenceable {
    val reference: String?
    fun dereference(r: String): IScribeable? = null
}

class ScriberReferencerWriter(
        private val inner: IScribeWriter,
        private val cache: MutableSet<IScribeReferenceable> = HashSet<IScribeReferenceable>()
        ): IScribeWriter by inner {

    private fun wrapUnscriber(outer: Unscriber): Unscriber {
        return fun(inner: IScribeWriter, scribeable: IScribeable?): Unit? {
            return outer(ScriberReferencerWriter(inner, cache), scribeable)
        }
     }

    override fun write(key: String, value: IScribeable?, unscriber: Unscriber) {

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
                cache.add(referenceable)
            }
        }
        // else, normal handling
        inner.write(key, value, wrapUnscriber(unscriber))
    }
    override fun write(key: String, value: List<Any?>?, unscriber: Unscriber) {
        inner.write(key, value, wrapUnscriber(unscriber))
    }
    override fun write(key: String, value: Map<String, Any?>?, unscriber: Unscriber) {
        inner.write(key, value, wrapUnscriber(unscriber))
    }
}
