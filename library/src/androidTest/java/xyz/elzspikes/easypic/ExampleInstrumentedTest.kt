package xyz.elzspikes.easypic

import android.support.test.InstrumentationRegistry
import org.junit.Test

import org.junit.Assert.*

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("xyz.elzspikes.easypic.test", appContext.packageName)
    }
}