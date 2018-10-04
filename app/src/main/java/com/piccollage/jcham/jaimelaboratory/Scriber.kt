package com.piccollage.jcham.jaimelaboratory

interface IScribeable {
    fun scribe(s: IScribeWriter)
}
typealias Scriber = (IScribeWriter, IScribeable?) -> Unit?
val ScriberDefault: Scriber = { writer: IScribeWriter, scribeable: IScribeable? ->
        scribeable?.scribe(writer)
    }
typealias Unscriber = (IScribeReader) -> IScribeable?
typealias ListUnscriber = (IScribeReader) -> List<Any?>

interface IScribeWriter {
    fun write(key: String, value: Int?)
    fun write(key: String, value: Float?)
    fun write(key: String, value: String?)
    fun write(key: String, value: Boolean?)
    fun write(key: String, value: IScribeable?,       scriber: Scriber = ScriberDefault)
    fun write(key: String, value: List<Any?>?,        scriber: Scriber = ScriberDefault)
    fun write(key: String, value: Map<String, Any?>?, scriber: Scriber = ScriberDefault)
}
interface IScribeReader {
    fun readInt(key: String): Int?
    fun readFloat(key: String): Float?
    fun readString(key: String): String?
    fun readBoolean(key: String): Boolean?
    fun read(key: String,     unscriber: Unscriber): IScribeable?
    fun readList(key: String, unscriber: Unscriber): List<Any?>?
    fun readMap(key: String,  unscriber: Unscriber): Map<String, Any?>?
}

class ListScribeable(val list: List<Any?>): IScribeable {
    override fun scribe(s: IScribeWriter) {
        list.forEachIndexed { index, value ->
            val key = index.toString()
            when(value) {
                is Int          -> s.write(key, value)
                is Float        -> s.write(key, value)
                is String       -> s.write(key, value)
                is Boolean      -> s.write(key, value)
                is IScribeable  -> s.write(key, value)
                is List<Any?>   -> s.write(key, value)
                is Map<*, *>    -> s.write(key, value as Map<String, Any?>)
                else            -> s.write(key, value.toString())
            }
        }
    }
    constructor(r: IScribeReader, unscriber: (IScribeReader) -> List<Any?>):
}
class MapScribeable(val map: Map<String, Any?>): IScribeable {
    override fun scribe(s: IScribeWriter) {
        map.forEach { (key, value) ->
            when(value) {
                is Int         -> s.write(key, value)
                is Float       -> s.write(key, value)
                is String      -> s.write(key, value)
                is Boolean     -> s.write(key, value)
                is IScribeable -> s.write(key, value)
                is List<Any?>  -> s.write(key, value)
                is Map<*, *>   -> s.write(key, value as Map<String, Any?>)
                else           -> s.write(key, value.toString())
            }
        }
    }
}

