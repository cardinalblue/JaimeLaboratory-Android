package com.piccollage.jcham.jaimelaboratory

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ObservableSet<E>(elements: Collection<E>) : HashSet<E>(elements) {
    constructor(): this(emptyList())

    var initialized = true
        // Hack that is needed because the primary constructor
        // is NOT run until AFTER the base class (HashSet) constructor,
        // but the base class constructor calls `add`, `remove`, etc.
        // so we need to be able to detect if we haven't been initialized yet.

    val added:   Observable<E> get() { return _added.hide() }
    val removed: Observable<E> get() { return _removed.hide() }

    private
    val _added: PublishSubject<E> by lazy {
        PublishSubject.create<E>()
    }

    private
    val _removed: PublishSubject<E> by lazy {
        PublishSubject.create<E>()
    }

    override fun add(element: E): Boolean {
        if (super.add(element)) {
            if (initialized) {
                _added.onNext(element)
            }
            return true
        }
        return false
    }
    override fun clear() {
        val elements = toList()
        super.clear()
        elements.forEach() {
            if (initialized) {
                _removed.onNext(it)
            }
        }
    }
    override fun remove(element: E): Boolean {
        if (super.remove(element)) {
            if (initialized) {
                _removed.onNext(element)
            }
            return true
        }
        return false
    }

    // ---- No need to override `addAll`, since `HashSet` implementation
    //      calls `add` (overridden above).
    //
    //    override fun addAll(elements: Collection<E>): Boolean {
    //        if (super.addAll(elements)) {
    //            elements.forEach { e -> _added.onNext(e) }
    //            return true
    //        }
    //        return false
    //    }

    // ---- Need to override `removeAll` because it doesn't call
    //      `remove` correctly (it seems) when there are extra items
    //      not already in the Set. But the original implementation
    //      does call `remove` overridden above.
    //
    override fun removeAll(elements: Collection<E>): Boolean {
        val removedElements = intersect(elements)
        return super.removeAll(removedElements)
    }

    // ---- Need to override `retainAll` because it doesn't call
    //      `remove` correctly (it seems).
    //
    override fun retainAll(retained: Collection<E>): Boolean {
        val removedElements = HashSet(this).apply { this.removeAll(retained) }
        if (super.retainAll(retained)) {
            removedElements.forEach { _removed.onNext(it) }
            return true
        }
        return false
    }

}


