package com.piccollage.jcham.jaimelaboratory

import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun `basic functionality`() {
        class K {
            var m: Int by Observator(100)
            var x: Int = 1
        }
        val k = K()

        assertNotNull(k::m.observable())
        assertNull(k::x.observable())

        // Check initial value
        assertEquals(100, k.m)

        // Observe its initial value
        val prop = k::m
        val tester = k::m.observable()!!.test()
        tester.assertValue(100)

        // Change its value
        k.m = 200
        assertEquals(200, k.m)
        tester.assertValues(100, 200)

        // Change its value again
        k.m = 300
        tester.assertValues(100, 200, 300)


    }

    fun `basic array`() {

    }

}
