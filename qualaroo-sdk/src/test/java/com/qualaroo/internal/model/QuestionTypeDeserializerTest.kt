package com.qualaroo.internal.model

import com.google.gson.JsonPrimitive
import org.junit.Assert
import org.junit.Test

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class QuestionTypeDeserializerTest {

    val deserializer = QuestionTypeDeserializer()

    @Test
    fun deserializes() {
        assertEquals(
                expected = QuestionType.RADIO,
                actual = deserializer.deserialize(JsonPrimitive("radio"), null, null)
        )
        assertEquals(
                expected = QuestionType.TEXT,
                actual = deserializer.deserialize(JsonPrimitive("text"), null, null)
        )
        assertEquals(
                expected = QuestionType.CHECKBOX,
                actual = deserializer.deserialize(JsonPrimitive("checkbox"), null, null)
        )
        assertEquals(
                expected = QuestionType.NPS,
                actual = deserializer.deserialize(JsonPrimitive("nps"), null, null)
        )
    }

    @Test
    fun `deserializes unknown questions`() {
        assertEquals(
                expected = QuestionType.UNKNOWN,
                actual = deserializer.deserialize(JsonPrimitive("gibberish something"), null, null)
        )
    }

    fun assertEquals(expected: Any, actual: Any) {
        Assert.assertEquals(expected, actual)
    }

}