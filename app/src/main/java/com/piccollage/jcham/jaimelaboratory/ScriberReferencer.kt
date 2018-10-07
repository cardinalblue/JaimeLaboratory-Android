package com.piccollage.jcham.jaimelaboratory

interface IScribeReferenceable {
    val reference: String?
    fun dereference(r: String): IScribeable? = null
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
