package com.piccollage.jcham.jaimelaboratory

interface IScribeable {
    fun scribe(s: IScribeWriter)
}
typealias ScribeableConstructor = (IScribeReader) -> IScribeable?

interface IScribeWriter {
    fun write(key: String, value: IScribeable?)                     : IScribeWriter
    fun write(key: String, value: List<IScribeable?>?)               : IScribeWriter
    fun write(key: String, value: Map<String, IScribeable?>?)        : IScribeWriter
    fun write(key: String, value: Int?)                             : IScribeWriter
    fun write(key: String, value: Float?)                           : IScribeWriter
    fun write(key: String, value: String?)                          : IScribeWriter
    fun write(key: String, value: Boolean?)                         : IScribeWriter
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

