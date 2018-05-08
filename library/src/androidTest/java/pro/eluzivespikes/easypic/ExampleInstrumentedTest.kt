package pro.eluzivespikes.easypic

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

        assertEquals("pro.eluzivespikes.easypic.test", appContext.packageName)
    }
}