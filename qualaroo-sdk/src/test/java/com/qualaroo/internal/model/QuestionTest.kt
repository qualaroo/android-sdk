package com.qualaroo.internal.model

import com.qualaroo.internal.model.TestModels.question
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("IllegalIdentifier")
class QuestionTest {

    @Test
    fun equals() {
        val question = question(
                id = 123,
                type = QuestionType.CHECKBOX
        )

        val questionWithSameIdButDifferentType = question(
                id = 123,
                type = QuestionType.RADIO
        )

        val questionWithSameTypeButDifferentId = question(
                id = 124,
                type = QuestionType.CHECKBOX
        )

        val totallyDifferentQuestion = question(
                id = 130,
                type = QuestionType.NPS
        )

        assertTrue(question == questionWithSameIdButDifferentType)
        assertTrue(question == question)
        assertFalse(question == questionWithSameTypeButDifferentId)
        assertFalse(question == totallyDifferentQuestion)
    }

    @Test
    fun `makes disable random flag more readable`() {
        val question = question(
                id = 5,
                type = QuestionType.RADIO,
                disableRandom = false
        )

        assertTrue(question.enableRandom())
    }
}
