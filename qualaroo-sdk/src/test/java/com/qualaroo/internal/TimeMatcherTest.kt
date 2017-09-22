package com.qualaroo.internal

import com.qualaroo.util.TestTimeProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TimeMatcherTest {

    val timeProvider = TestTimeProvider()
    val timeMatcher = TimeMatcher(timeProvider)

    @Test
    fun enoughTimePassed() {
        assertFalse(timeMatcher.enoughTimePassed(0))

        timeProvider.setCurrentTime(TimeUnit.DAYS.toMillis(3))
        assertTrue(timeMatcher.enoughTimePassed(0))
    }

}