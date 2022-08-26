package com.qualaroo.internal.model

import com.qualaroo.internal.model.TestModels.message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageTest {

    @Test
    fun equals() {
        val message = message(
                id = 1
        )

        val sameMessage = message(
                id = 1
        )

        val otherMessage = message(
                id = 2
        )

        assertTrue(message == message)
        assertTrue(message == sameMessage)
        assertFalse(message == otherMessage)
    }

}