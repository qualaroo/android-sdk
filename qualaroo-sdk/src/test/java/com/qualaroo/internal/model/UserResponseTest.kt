package com.qualaroo.internal.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserResponseTest {


    @Test
    fun equals() {
        val first = UserResponse.Builder(123)
                .addChoiceAnswer(1)
                .addChoiceAnswerWithComment(2, "comment")
                .addTextAnswer("text")
                .build()
        val second = UserResponse.Builder(123)
                .addChoiceAnswer(1)
                .addChoiceAnswerWithComment(2, "comment")
                .addTextAnswer("text")
                .build()

        assertEquals(first, second)

        val third = UserResponse.Builder(123)
                .addChoiceAnswer(1)
                .addChoiceAnswerWithComment(2, "comment")
                .addTextAnswer("text")
                .build()

        val fourth = UserResponse.Builder(321)
                .addChoiceAnswer(1)
                .addChoiceAnswerWithComment(2, "comment")
                .addTextAnswer("text")
                .build()

        assertNotEquals(third, fourth)
    }

}
