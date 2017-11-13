package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.qualaroo.util.TimeProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanPrivate")
class TimeMatcherTest {

    private val timeProvider = mock<TimeProvider>()
    val timeMatcher = TimeMatcher(TimeUnit.DAYS.toMillis(3))

    @Test
    fun enoughTimePassed() {
        timeMatcher.timeProvider = timeProvider

        whenever(timeProvider.currentTimeMillis()).thenReturn(TimeUnit.DAYS.toMillis(7))
        assertFalse(timeMatcher.enoughTimePassedFrom(TimeUnit.DAYS.toMillis(5)))
        assertTrue(timeMatcher.enoughTimePassedFrom(TimeUnit.DAYS.toMillis(4)))
    }

}
