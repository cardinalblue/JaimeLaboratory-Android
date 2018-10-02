package com.piccollage.jcham.jaimelaboratory

interface IScribeReferenceable {
    val reference: String?
    fun dereference(r: String): IScribeable? = null
}
interface IScribeable {
    fun scribe(s: IScribeWriter)
}
typealias ScribeableConstructor = (IScribeReader) -> IScribeable?

interface IScribeWriter {
    fun write(key: String, value: Int?)
    fun write(key: String, value: Float?)
    fun write(key: String, value: String?)
    fun write(key: String, value: Boolean?)
    fun write(key: String, value: IScribeable?): IScribeWriter?
    fun write(key: String, value: List<IScribeable?>?): IScribeWriter?
    fun write(key: String, value: Map<String, IScribeable?>?): IScribeWriter?
}
interface IScribeReader {
    fun read_Int(key: String): Int?
    fun read_Float(key: String): Float?
    fun read_String(key: String): String?
    fun read_Boolean(key: String): Boolean?
    fun read(key: String, scribeable: ScribeableConstructor): IScribeable?
    fun read_List(key: String, scribeable: ScribeableConstructor): List<IScribeable?>?
    fun read_Map(key: String, scribeable: ScribeableConstructor): Map<String, IScribeable?>?
}


class ScribeWriter(val inner: IScribeWriter): IScribeWriter by inner {
    override fun write(key: String, value: IScribeable?): IScribeWriter? {
        inner.write(key, value)?.let { writer ->
            value?.scribe(ScribeWriter(writer))
        }
        return null
    }
    override fun write(key: String, value: List<IScribeable?>?): IScribeWriter? {
        val writer = inner.write(key, value)
        if (writer != null && value != null)
            value.map {
                ScribeWriter(writer).write("_", it)
            }
        return null
    }
    override fun write(key: String, value: Map<String, IScribeable?>?): IScribeWriter? {
        val w1 = inner.write(key, value)
        if (w1 != null && value != null) {
            val writer = ScribeWriter(w1)
            value.forEach { (subkey, subvalue) ->
                writer.write(subkey, subvalue)
            }
        }
        return null
    }
}

