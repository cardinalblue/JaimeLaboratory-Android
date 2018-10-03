package com.piccollage.jcham.jaimelaboratory

fun <T> MutableList<T>.removeLast(): T {
    val i = count() - 1
    val last: T = elementAt(i)
    removeAt(i)
    return last
}

class JsonScribeWriter: IScribeWriter {
    val result: MutableMap<String, Any?> = mutableMapOf<String, Any?>()

    override fun write(key: String, value: Int?) {
        result.set(key, value) }
    override fun write(key: String, value: Float?) {
        result.set(key, value) }
    override fun write(key: String, value: String?) {
        result.set(key, value) }
    override fun write(key: String, value: Boolean?) {
        result.set(key, value) }

    override fun write(key: String, value: IScribeable?, scriber: Scriber) {
        result[key] = value?.let { JsonScribeWriter()
                        .apply { scriber(this, value) }
                        .result
                        }
    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        result[key] = value?.let{ JsonListScribeWriter()
                        .apply { scriber(this, ListScribeable(value)) }
                        .result
                        }

    }
    override fun write(key: String, value: Map<String, Any?>?, scriber: Scriber) {
        result[key] = value?.let { JsonScribeWriter()
                        .apply { scriber(this, MapScribeable(value)) }
                        .result
                        }
    }

}
class JsonListScribeWriter: IScribeWriter {
    val result = mutableListOf<Any?>()
    override fun write(key: String, value: Int?) {
        result.add(value) }
    override fun write(key: String, value: Float?) {
        result.add(value) }
    override fun write(key: String, value: String?) {
        result.add(value) }
    override fun write(key: String, value: Boolean?) {
        result.add(value) }
    override fun write(key: String, value: IScribeable?, scriber: Scriber) {
        result.add(
            value?.let { JsonScribeWriter()
                .apply { scriber(this, value) }
                .result
            })
    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        result.add(
            value?.let { JsonListScribeWriter()
                .apply { scriber(this, ListScribeable(value)) }
                .result
            })
    }
    override fun write(key: String, value: Map<String, Any?>?, scriber: Scriber) {
        result.add(
            value?.let { JsonScribeWriter()
                .apply { scriber(this, MapScribeable(value)) }
                .result
            })
    }
}

class ListScribeable(val list: List<Any?>): IScribeable {
    override fun scribe(s: IScribeWriter) {
        list.forEachIndexed { index, value ->
            val key = index.toString()
            when(value) {
            is Int                  -> s.write(key, value)
            is Float                -> s.write(key, value)
            is String               -> s.write(key, value)
            is Boolean              -> s.write(key, value)
            is IScribeable          -> s.write(key, value)
            is List<Any?>           -> s.write(key, value)
            is Map<*, *>            -> s.write(key, value as Map<String, Any?>)
            else                    -> {}
            }
        }
    }
}
class MapScribeable(val map: Map<String, Any?>): IScribeable {
    override fun scribe(s: IScribeWriter) {
        map.forEach { (key, value) ->
            when(value) {
                is Int                  -> s.write(key, value)
                is Float                -> s.write(key, value)
                is String               -> s.write(key, value)
                is Boolean              -> s.write(key, value)
                is IScribeable          -> s.write(key, value)
                is List<Any?>           -> s.write(key, value)
                is Map<*, *>            -> s.write(key, value as Map<String, Any?>)
                else                    -> {}
            }
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