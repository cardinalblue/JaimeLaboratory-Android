package com.piccollage.jcham.jaimelaboratory

import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty
import io.reactivex.Observable
import io.reactivex.rxkotlin.*
import io.reactivex.subjects.BehaviorSubject
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

class Observator<T>(defaultValue: T) {
    val defaultValue = defaultValue

    // ---- Delegate interface
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return this.subject.getValue() ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.subject.onNext(value)
    }

    // ---- Implementation
    val subject: BehaviorSubject<T> by lazy {
        BehaviorSubject.createDefault(defaultValue)
    }

    // ---- Public interface:

    // Get the Observable
    //
    fun observable(): Observable<T> {
        return this.subject.hide()

    }
}

@Target(AnnotationTarget.PROPERTY)
annotation class Observable() {


}

fun <R> KProperty0<R>.observable(): Observable<R>? {
    isAccessible = true
    val delegate = this.getDelegate() as? Observator<R>
    return delegate?.observable() as? Observable<R>
}

