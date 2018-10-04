package com.piccollage.jcham.jaimelaboratory

fun <T> MutableList<T>.removeLast(): T {
    val i = count() - 1
    val last: T = elementAt(i)
    removeAt(i)
    return last
}

class JsonScribeWriter: IScribeWriter {
    val result: MutableMap<String, Any?> = mutableMapOf<String, Any?>()

    override fun write(key: String, value: Int?)     { result.set(key, value) }
    override fun write(key: String, value: Float?)   { result.set(key, value) }
    override fun write(key: String, value: String?)  { result.set(key, value) }
    override fun write(key: String, value: Boolean?) { result.set(key, value) }

    override fun write(key: String, value: IScribeable?, scriber: Scriber) {
        result[key] = value.iff {
                        JsonScribeWriter()
                            .apply { scriber(this, value) }
                            .result
                        }
    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        result[key] = value?.iff {
                        JsonListScribeWriter()
                            .apply { scriber(this, ListScribeable(value)) }
                            .result
                        }
    }
    override fun write(key: String, value: Map<String, Any?>?, scriber: Scriber) {
        result[key] = value?.iff {
                        JsonScribeWriter()
                            .apply { scriber(this, MapScribeable(value)) }
                            .result
                        }
    }

}
class JsonListScribeWriter: IScribeWriter {
    val result = mutableListOf<Any?>()
    override fun write(key: String, value: Int?) {              result.add(value) }
    override fun write(key: String, value: Float?) {            result.add(value) }
    override fun write(key: String, value: String?) {           result.add(value) }
    override fun write(key: String, value: Boolean?) {          result.add(value) }
    override fun write(key: String, value: IScribeable?, scriber: Scriber) {
        result.add(
            value?.iff { JsonScribeWriter()
                .apply { scriber(this, value) }
                .result
            })
    }
    override fun write(key: String, value: List<Any?>?, scriber: Scriber) {
        result.add(
            value?.iff { JsonListScribeWriter()
                .apply { scriber(this, ListScribeable(value)) }
                .result
            })
    }
    override fun write(key: String, value: Map<String, Any?>?, scriber: Scriber) {
        result.add(
            value?.iff { JsonScribeWriter()
                .apply { scriber(this, MapScribeable(value)) }
                .result
            })
    }
}


class JsonScribeReader(val json: Map<String, Any?>): IScribeReader {

    // ==== Reading interface
    override fun readInt(key: String): Int?            { return json[key] as? Int }
    override fun readFloat(key: String): Float?        { return json[key] as? Float }
    override fun readString(key: String): String?      { return json[key] as? String }
    override fun readBoolean(key: String): Boolean?    { return json[key] as? Boolean }
    override fun read(key: String, unscriber: Unscriber): IScribeable? {
        val map = json[key] as? Map<String, Any?>
        return map?.iff {
            unscriber(JsonScribeReader(map))
        }
    }
    override fun readList(key: String, unscriber: ListUnscriber): List<Any?>? {
        val list = json[key] as? List<String, Any?>
        return list?.iff {
            unscriber(JsonListScribeReader(list))
        }
    }
    override fun readList(key: String, unscriber: Unscriber): List<Any?>? {
        val list = json[key] as? List<String, Any?>
        return list?.iff {
            unscriber(JsonListScribeReader(list))
        }
    }
    override fun readMap(key: String, unscriber: Unscriber): Map<String, IScribeable?>? {
        @Suppress("UNCHECKED_CAST")
        val map = json[key] as? Map<String, Any>
        return map?.mapValues { e -> extract(e.value, scribeable) }
    }

}