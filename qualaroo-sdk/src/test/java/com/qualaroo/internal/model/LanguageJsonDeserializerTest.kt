package com.qualaroo.internal.model

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
@Suppress("MemberVisibilityCanPrivate")
class LanguageJsonDeserializerTest {

    val deserializer = LanguageJsonDeserializer()

    @Test
    fun deserializes() {
        val json = JsonPrimitive("us")

        val language = deserializer.deserialize(json, null, null)

        assertEquals("us", language.value())
    }

    @Test
    fun deserializesObjectsAsUnknownLanguage() {
        val json = JsonObject()
        json.add("someObjectInside", JsonObject())

        val language = deserializer.deserialize(json, null, null)

        assertEquals(Language.UNKNOWN, language)
    }

}

