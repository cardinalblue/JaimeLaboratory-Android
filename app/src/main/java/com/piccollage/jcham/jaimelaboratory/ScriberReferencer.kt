package com.piccollage.jcham.jaimelaboratory

/*
class ScriberReferenceWriter(
        val inner: IScribeWriter,
        val cache: Set<IScribeReferenceable> = HashSet<IScribeReferenceable>()
        ): IScribeWriter by inner {

    override fun write(key: String, value: IScribeable?): IScribeWriter? {
        // See if referenceable
        (value as? IScribeReferenceable)?.let { referenceable ->
            // See if in cache
            if (cache.contains(referenceable)) {
                // See if it has the reference
                referenceable.reference?.let { reference ->
                    // Write the reference
                    inner.write(key, value)?.let { writer ->
                        writer.write("\$ref", reference)
                    }
                }
                return null
            }
        }
        // else, normal handling
        inner.write(key, value)?.let { writer ->
            value?.scribe(ScriberReferenceWriter(writer, cache))
        }
        return this
    }
    override fun write(key: String, value: List<IScribeable?>?): IScribeWriter? {
        val writer = inner.write(key, value)
        return writer.if { ScriberReferenceWriter(writer, cache) }
    }
    override fun write(key: String, value: Map<String, IScribeable?>?): IScribeWriter? {
        val writer = inner.write(key, value)
        return ScriberReferenceWriter(writer, cache)
    }



}
*/