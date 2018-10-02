package com.piccollage.jcham.jaimelaboratory

interface IScribeReferenceable {
    val reference: String?
    fun dereference(r: String): IScribeable? = null
}
interface IScribeable {
    fun scribe(s: IScribeWriter)
}
typealias ScribeableConstructor = (IScribeReader) -> IScribeable?
typealias Scriber = (IScribeable?, IScribeWriter) -> Unit?
val ScriberDefault = { scribeable: IScribeable?, writer: IScribeWriter ->
    scribeable?.scribe(writer)
}

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
    fun read_Int(key: String): Int?
    fun read_Float(key: String): Float?
    fun read_String(key: String): String?
    fun read_Boolean(key: String): Boolean?
    fun read(key: String, scribeable: ScribeableConstructor): IScribeable?
    fun read_List(key: String, scribeable: ScribeableConstructor): List<IScribeable?>?
    fun read_Map(key: String, scribeable: ScribeableConstructor): Map<String, IScribeable?>?
}

