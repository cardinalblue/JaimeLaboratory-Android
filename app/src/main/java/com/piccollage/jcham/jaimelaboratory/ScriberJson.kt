package com.piccollage.jcham.jaimelaboratory

fun <T> MutableList<T>.removeLast(): T {
    val i = count() - 1
    val last: T = elementAt(i)
    removeAt(i)
    return last
}

class JsonScribeWriter(val result: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): IScribeWriter {

    override fun write(key: String, value: Int?) {
        result.set(key, value) }
    override fun write(key: String, value: Float?) {
        result.set(key, value) }
    override fun write(key: String, value: String?) {
        result.set(key, value) }
    override fun write(key: String, value: Boolean?) {
        result.set(key, value) }

    fun scribe(scriber: Scriber, s: IScribeable?) = HashMap<String, Any?>().apply {
        scriber(s, JsonScribeWriter(this))
    }

    override fun write(key: String, value: IScribeable?, scriber: Scriber) {
        result[key] = scribe(scriber, value)

    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        result[key] = value?.map {
            if (it is IScribeable)
                scribe(scriber, it)
            else
                it
        }
    }
    override fun write(key: String, value: Map<String, Any?>?, scriber: Scriber) {
        result[key] = value?.mapValues { e ->
            val v = e.value
            if (v is IScribeable)
                scribe(scriber, v)
            else
                e.value
        }
    }

}


typealias JsonMap = Map<String, Any?>
class JsonScribeReader(json: JsonMap, var inner: IScribeReader? = null): IScribeReader {
    private val jsonStack = mutableListOf<JsonMap>(json)
    private val json
        get() = jsonStack.last()

    private fun extract(v: Any?, scribeable: ScribeableConstructor): IScribeable? {
        @Suppress("UNCHECKED_CAST")
        val s: JsonMap? = v as? JsonMap?
        s ?: return null
        jsonStack.add(s)
        return scribeable(inner ?: this).also {
            jsonStack.removeLast()
        }
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