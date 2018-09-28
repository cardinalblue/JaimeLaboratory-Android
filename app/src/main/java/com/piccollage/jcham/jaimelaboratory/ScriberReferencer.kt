package com.piccollage.jcham.jaimelaboratory

class ScriberReferenceWriter(val inner: IScribeWriter): IScribeWriter by inner {

    override fun write(key: String, value: Int?)                             : IScribeWriter {
        println("write Int $value")
        return inner.write(key, value)
        }
    override fun write(key: String, value: Float?)                           : IScribeWriter {
        println("write Float $value")
        return inner.write(key, value)
        }
    override fun write(key: String, value: String?)                          : IScribeWriter {
        println("write String $value")
        return inner.write(key, value)
        }
    override fun write(key: String, value: Boolean?)                         : IScribeWriter {
        println("write Boolean $value")
        return inner.write(key, value)
        }
    override fun write(key: String, value: IScribeable?, subwriter: IScribeWriter): IScribeWriter {
        println("write $value")
        return inner.write(key, value, this)
    }
    override fun write(key: String, value: List<IScribeable?>?, subwriter: IScribeWriter): IScribeWriter {
        println("write List $value")
        return inner.write(key, value, this)
    }
    override fun write(key: String, value: Map<String, IScribeable?>?, subwriter: IScribeWriter): IScribeWriter {
        println("write Map $value")
        return inner.write(key, value, this)
    }

}