package com.piccollage.jcham.jaimelaboratory

fun <T> MutableList<T>.removeLast(): T {
    val i = count() - 1
    val last: T = elementAt(i)
    removeAt(i)
    return last
}
typealias JsonMap = MutableMap<String, Any?>
class JsonScribeWriter: IScribeWriter {
    val result
        get() = resultStack.last()
    private val resultStack = mutableListOf<JsonMap>(HashMap<String, Any?>())

    private fun extract(s: IScribeable?): JsonMap {
        resultStack.add(HashMap<String, Any?>())
        s?.scribe(this)
        return resultStack.removeLast()
    }

    override fun write(key: String, value: Int?)        : IScribeWriter {
        result[key] = value; return this }
    override fun write(key: String, value: Float?)      : IScribeWriter {
        result[key] = value; return this }
    override fun write(key: String, value: String?)     : IScribeWriter {
        result[key] = value; return this }
    override fun write(key: String, value: Boolean?)    : IScribeWriter {
        result[key] = value; return this }
    override fun write(key: String, value: IScribeable?): IScribeWriter {
        result[key] = if (value != null) extract(value)
        else null
        return this
    }
    override fun write(key: String, value: List<IScribeable?>?): IScribeWriter {
        result[key] = value?.map { extract(it) }
        return this
    }
    override fun write(key: String, value: Map<String, IScribeable?>?): IScribeWriter {
        result[key] = value?.mapValues { extract(it.value) }
        return this
    }
}
class JsonScribeReader(json: JsonMap): IScribeReader {
    private val jsonStack = mutableListOf<JsonMap>(json)
    private val json
        get() = jsonStack.last()

    private fun extract(v: Any?, scribeable: ScribeableConstructor): IScribeable? {
        @Suppress("UNCHECKED_CAST")
        val s: JsonMap? = v as? JsonMap?
        s ?: return null
        jsonStack.add(s)
        return scribeable(this).also { jsonStack.removeLast() }
    }
    // ==== Reading interface
    override fun read_Int(key: String): Int?            { return json[key] as? Int }
    override fun read_Float(key: String): Float?        { return json[key] as? Float }
    override fun read_String(key: String): String?      { return json[key] as? String }
    override fun read_Boolean(key: String): Boolean?    { return json[key] as? Boolean }
    override fun read(key: String, scribeable: ScribeableConstructor): IScribeable? {
        return extract(json[key], scribeable)
    }
    override fun read_List(key: String, scribeable: ScribeableConstructor): List<IScribeable?>? {
        @Suppress("UNCHECKED_CAST")
        val list = json[key] as? List<Map<String, Any?>>
        return list?.map { i -> extract(i, scribeable) }
    }
    override fun read_Map(key: String, scribeable: ScribeableConstructor): Map<String, IScribeable?>? {
        @Suppress("UNCHECKED_CAST")
        val map = json[key] as? Map<String, Any>
        return map?.mapValues { e -> extract(e.value, scribeable) }
    }

}