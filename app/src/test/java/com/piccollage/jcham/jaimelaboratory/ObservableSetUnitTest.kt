package com.piccollage.jcham.jaimelaboratory

import org.junit.Test
import org.junit.Assert.*

class ObservableSetUnitTest {

    @Test
    fun `constructs`() {
        assertNotNull(ObservableSet<Int>().added)
        assertNotNull(ObservableSet(listOf(1, 2, 3)).removed)
    }

    @Test
    fun `basic functionality`() {
        val m = ObservableSet<Int>(listOf(1, 2, 3))

        val added   = m.added.test()
        val removed = m.removed.test()

        assertArrayEquals(arrayOf(1, 2, 3), m.toArray())
        added.assertNoValues()
        removed.assertNoValues()

        assert(m.add(4))
        assertArrayEquals(arrayOf(1, 2, 3, 4), m.toArray())
        added.assertValues(4)
        removed.assertNoValues()

        assert(m.add(5))
        assertArrayEquals(arrayOf(1, 2, 3, 4, 5), m.toArray())
        added.assertValues(4, 5)
        removed.assertNoValues()

        assert(m.add(6))
        assert(m.remove(2))
        assertFalse(m.remove(8))
        assertArrayEquals(arrayOf(1, 3, 4, 5, 6), m.toArray())
        added.assertValues(4, 5, 6)
        removed.assertValues(2)

        m.clear()
        assertArrayEquals(arrayOf(), m.toArray())
        added.assertValues(4, 5, 6)
        removed.assertValues(2, 1, 3, 4, 5, 6)

        // addAll
        assert(m.addAll(listOf(7, 8, 9)))
        assertArrayEquals(arrayOf(7, 8, 9), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9)
        removed.assertValues(2, 1, 3, 4, 5, 6)

        // Repeat `add`
        assertFalse(m.add(7))
        assertFalse(m.addAll(listOf(8)))
        assertArrayEquals(arrayOf(7, 8, 9), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9)
        removed.assertValues(2, 1, 3, 4, 5, 6)

        // `removeAll` with exact (special case)
        assert(m.removeAll(listOf(8)))
        assertArrayEquals(arrayOf(7, 9), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9)
        removed.assertValues(2, 1, 3, 4, 5, 6, 8)

        // `removeAll` with extra ones (special case)
        assert(m.removeAll(listOf(8, 9, 10, 11)))
        assertArrayEquals(arrayOf(7), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9)
        removed.assertValues(2, 1, 3, 4, 5, 6, 8, 9)

        // `add` some more
        assert(m.add(10))
        assertArrayEquals(arrayOf(7, 10), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9, 10)
        removed.assertValues(2, 1, 3, 4, 5, 6, 8, 9)

        // `retainAll`
        assert(m.retainAll(listOf(10, 11, 12)))
        assertArrayEquals(arrayOf(10), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9, 10)
        removed.assertValues(2, 1, 3, 4, 5, 6, 8, 9, 7)

        // `retainAll` again, exact
        m.add(13)
        assert(m.retainAll(listOf(10)))
        assertArrayEquals(arrayOf(10), m.toArray())
        added.assertValues(4, 5, 6, 7, 8, 9, 10, 13)
        removed.assertValues(2, 1, 3, 4, 5, 6, 8, 9, 7, 13)



    }

}
