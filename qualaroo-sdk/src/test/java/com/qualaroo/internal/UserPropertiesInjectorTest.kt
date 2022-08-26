package com.qualaroo.internal

import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.qscreen
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.util.InMemorySettings
import org.junit.Assert.*
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanBePrivate")
class UserPropertiesInjectorTest {

    val userInfo = UserInfo(InMemorySettings(), InMemoryLocalStorage())
    val injector = UserPropertiesInjector(userInfo)

    @Test
    fun `injects questions`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 1, title = "Hello \${name}", description = "This is your last name: \${last_name}"),
                                        question(id = 2, title = "Are you still there \${name} \${last_name}?"),
                                        question(id = 3, title = "\${name}? Are you still there, \${name}?"),
                                        question(id = 4, title = "No custom properties here"),
                                        question(id = 5, title = "\${missing_property} <- this?")
                                )
                        )
                )
        )

        userInfo.setUserProperty("name", "Adam")
        userInfo.setUserProperty("last_name", "Tester")

        val result = injector.injectCustomProperties(survey, language("en"))

        val questions = result.spec().questionList()[language("en")]!!
        assertEquals("Hello Adam", questions[0].title())
        assertEquals("This is your last name: Tester", questions[0].description())
        assertEquals("Are you still there Adam Tester?", questions[1].title())
        assertEquals("Adam? Are you still there, Adam?", questions[2].title())
        assertEquals("No custom properties here", questions[3].title())
        assertEquals(" <- this?", questions[4].title())
    }

    @Test
    fun `injects messages`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(id = 1, description = "Hello \${name}"),
                                        message(id = 2, description = "Are you still there \${name} \${last_name}?"),
                                        message(id = 3, description = "\${name}? Are you still there, \${name}?"),
                                        message(id = 4, description = "No custom properties here?"),
                                        message(id = 5, description = "\${missing_property} <- this?")
                                )
                        )
                )
        )

        userInfo.setUserProperty("name", "Adam")
        userInfo.setUserProperty("last_name", "Tester")

        val result = injector.injectCustomProperties(survey, language("en"))

        val messages = result.spec().msgScreenList()[language("en")]!!
        assertEquals("Hello Adam", messages[0].description())
        assertEquals("Are you still there Adam Tester?", messages[1].description())
        assertEquals("Adam? Are you still there, Adam?", messages[2].description())
        assertEquals("No custom properties here?", messages[3].description())
        assertEquals(" <- this?", messages[4].description())
    }

    @Test
    fun `injects qscreens`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        qscreenList = mapOf(
                                language("en") to listOf(
                                        qscreen(id = 1, description = "Hello \${name}"),
                                        qscreen(id = 2, description = "Are you still there \${name} \${last_name}?"),
                                        qscreen(id = 3, description = "\${name}? Are you still there, \${name}?"),
                                        qscreen(id = 4, description = "No custom properties here?"),
                                        qscreen(id = 5, description = "\${missing_property} <- this?")
                                )
                        )
                )
        )

        userInfo.setUserProperty("name", "Adam")
        userInfo.setUserProperty("last_name", "Tester")

        val result = injector.injectCustomProperties(survey, language("en"))

        val qscreens = result.spec().qscreenList()[language("en")]!!
        assertEquals("Hello Adam", qscreens[0].description())
        assertEquals("Are you still there Adam Tester?", qscreens[1].description())
        assertEquals("Adam? Are you still there, Adam?", qscreens[2].description())
        assertEquals("No custom properties here?", qscreens[3].description())
        assertEquals(" <- this?", qscreens[4].description())
    }

    @Test
    fun `checks whether a survey can be injected`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 1, title = "Hello \${name}", description = "This is your last name: \${last_name}"),
                                        question(id = 2, title = "Are you still there \${name} \${last_name}?"),
                                        question(id = 3, title = "\${name}? Are you still there, \${name}?")
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(id = 1, description = "Hello \${name}"),
                                        message(id = 2, description = "Are you still there \${name} \${last_name}?"),
                                        message(id = 3, description = "\${name}? Are you still there, \${name}?")
                                )
                        ),
                        qscreenList = mapOf(
                                language("en") to listOf(
                                        qscreen(id = 1, description = "Hello \${name}"),
                                        qscreen(id = 2, description = "Are you still there \${name} \${last_name}?"),
                                        qscreen(id = 3, description = "\${name}? Are you still there, \${name}?")
                                )
                        )
                )
        )

        assertFalse(injector.canInjectAllProperties(survey))

        userInfo.setUserProperty("name", "Adam")

        assertFalse(injector.canInjectAllProperties(survey))

        userInfo.setUserProperty("last_name", "Tester")

        assertTrue(injector.canInjectAllProperties(survey))
    }

}
