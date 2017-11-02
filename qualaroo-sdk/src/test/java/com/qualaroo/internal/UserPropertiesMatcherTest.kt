package com.qualaroo.internal

import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.util.InMemorySettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class UserPropertiesMatcherTest {

    val localStorage = InMemoryLocalStorage()
    val userInfo = UserInfo(InMemorySettings(), localStorage)
    private val matcher = UserPropertiesMatcher(userInfo)

    @Test
    fun `matches basic cases`() {
        assertTrue(matcher.doesUserPropertiesMatch(null))
        assertTrue(matcher.doesUserPropertiesMatch(""))
        assertTrue(matcher.doesUserPropertiesMatch(" "))
        assertTrue(matcher.doesUserPropertiesMatch("\"something\""))
        assertTrue(matcher.doesUserPropertiesMatch("2"))
        assertTrue(matcher.doesUserPropertiesMatch("2==2"))
        assertFalse(matcher.doesUserPropertiesMatch("2 > 2"))
        assertFalse(matcher.doesUserPropertiesMatch("2 != 2"))
        assertTrue(matcher.doesUserPropertiesMatch("\"something\" || 2 == 2"))
        assertTrue(matcher.doesUserPropertiesMatch("\"something\" && 2 == 2"))
    }

    @Test
    fun `returns false when param is missing`() {
        assertFalse(matcher.doesUserPropertiesMatch("something"))
    }

    @Test
    fun `ignore empty spaces`() {
        assertTrue(matcher.doesUserPropertiesMatch("1==1"))
        assertTrue(matcher.doesUserPropertiesMatch("1 == 1"))
        assertTrue(matcher.doesUserPropertiesMatch("  1==     1"))
    }

    @Test
    fun `matches based on user properties`() {
        assertFalse(matcher.doesUserPropertiesMatch("premium==true"))

        userInfo.setUserProperty("premium", "true")
        assertTrue(matcher.doesUserPropertiesMatch("premium==true"))

        assertFalse(matcher.doesUserPropertiesMatch("premium == true && age > 18"))
        userInfo.setUserProperty("age", "18")
        assertFalse(matcher.doesUserPropertiesMatch("premium == true && age > 18"))
        userInfo.setUserProperty("age", "19")
        assertTrue(matcher.doesUserPropertiesMatch("premium == true && age > 18"))

        assertFalse(matcher.doesUserPropertiesMatch("(premium==true && age > 18) && name=\"Joe\""))

        userInfo.setUserProperty("name", "Joe")
        assertTrue(matcher.doesUserPropertiesMatch("(premium==true && age > 18) && name==\"Joe\""))

        userInfo.setUserProperty("age", "16")
        assertFalse(matcher.doesUserPropertiesMatch("((premium==true && age > 18) && name==\"Joe\") || job == \"ceo\""))

        userInfo.setUserProperty("job", "ceo")
        assertTrue(matcher.doesUserPropertiesMatch("((premium==true && age > 18) && name==\"Joe\") || job == \"ceo\""))
    }

    @Test
    fun `uses customMap field to extract required properties`() {
        val survey = survey(
                id = 10,
                spec = spec(
                        requireMap = requireMap(
                                customMap = "premium==true"
                        )
                )
        )
        assertFalse(matcher.matches(survey))

        userInfo.setUserProperty("premium", "true")
        assertTrue(matcher.matches(survey))
    }

}
