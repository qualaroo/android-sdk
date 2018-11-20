package com.qualaroo.internal.network

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.qualaroo.util.TimeProvider
import org.junit.Assert.*
import org.junit.Test

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class CacheTest {

    val TIME_LIMIT = 1000L
    val timeProvider = mock<TimeProvider>()

    private val tested = Cache<Foo>(timeProvider, TIME_LIMIT)

    @Test
    fun `stores values`() {
        val value = Foo(1)
        tested.put(value)

        assertEquals(value, tested.get())
    }

    @Test
    fun `isInvalid`() {
        assertTrue(tested.isInvalid)

        tested.put(null)
        assertTrue(tested.isInvalid)

        tested.put(Foo(1))
        assertFalse(tested.isInvalid)
    }

    @Test
    fun `isStale`() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(0)
        tested.put(Foo(1))

        assertFalse(tested.isStale)

        whenever(timeProvider.currentTimeMillis()).thenReturn(TIME_LIMIT - 1)
        assertFalse(tested.isStale)

        whenever(timeProvider.currentTimeMillis()).thenReturn(TIME_LIMIT + 1)
        assertTrue(tested.isStale)
    }

    class Foo(val id: Int)
}
