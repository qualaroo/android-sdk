package com.qualaroo.internal.model

import com.qualaroo.internal.model.TestModels.answer
import org.junit.Assert
import org.junit.Test

@Suppress("IllegalIdentifier")
class AnswerTest {

    @Test
    fun equals() {
        val answer = answer(
                id = 123
        )

        val differentAnswerWithSameId = answer(
                id = answer.id()
        )

        val answerWithDifferentId = answer(
                id = 130
        )

        Assert.assertTrue(answer == answer)
        Assert.assertTrue(answer == differentAnswerWithSameId)
        Assert.assertTrue(answer != answerWithDifferentId)
    }
}