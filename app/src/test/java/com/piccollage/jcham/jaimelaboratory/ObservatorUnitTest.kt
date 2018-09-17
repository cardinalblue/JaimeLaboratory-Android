package com.piccollage.jcham.jaimelaboratory

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ObservatorUnitTest {

    @Test
    fun `basic functionality`() {
        class K {
            var p: Int by Observator(100)
            var x: Int = 1
        }
        val k = K()

        assertNotNull(k::p.observable())
        assertNull(k::x.observable())

        // Check initial value
        assertEquals(100, k.p)

        // Observe its initial value
        val prop = k::p
        val tester = k::p.observable()!!.test()
        tester.assertValue(100)

        // Change its value
        k.p = 200
        assertEquals(200, k.p)
        tester.assertValues(100, 200)

        // Change its value again
        k.p = 300
        tester.assertValues(100, 200, 300)


    }

    fun `basic array`() {

    }

}

