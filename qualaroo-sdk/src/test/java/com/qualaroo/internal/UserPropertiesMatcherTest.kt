package com.qualaroo.internal

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
        assertTrue(matcher.match(null))
        assertTrue(matcher.match(""))
        assertTrue(matcher.match(" "))
        assertTrue(matcher.match("\"something\""))
        assertTrue(matcher.match("2"))
        assertTrue(matcher.match("2==2"))
        assertFalse(matcher.match("2 > 2"))
        assertFalse(matcher.match("2 != 2"))
        assertTrue(matcher.match("\"something\" || 2 == 2"))
        assertTrue(matcher.match("\"something\" && 2 == 2"))
    }

    @Test
    fun `returns false when param is missing`() {
        assertFalse(matcher.match("something"))
    }

    @Test
    fun `ignore empty spaces`() {
        assertTrue(matcher.match("1==1"))
        assertTrue(matcher.match("1 == 1"))
        assertTrue(matcher.match("  1==     1"))
    }

    @Test
    fun `matches based on user properties`() {
        assertFalse(matcher.match("premium==true"))

        userInfo.setUserProperty("premium", "true")
        assertTrue(matcher.match("premium==true"))

        assertFalse(matcher.match("premium == true && age > 18"))
        userInfo.setUserProperty("age", "18")
        assertFalse(matcher.match("premium == true && age > 18"))
        userInfo.setUserProperty("age", "19")
        assertTrue(matcher.match("premium == true && age > 18"))

        assertFalse(matcher.match("(premium==true && age > 18) && name=\"Joe\""))

        userInfo.setUserProperty("name", "Joe")
        assertTrue(matcher.match("(premium==true && age > 18) && name==\"Joe\""))

        userInfo.setUserProperty("age", "16")
        assertFalse(matcher.match("((premium==true && age > 18) && name==\"Joe\") || job == \"ceo\""))

        userInfo.setUserProperty("job", "ceo")
        assertTrue(matcher.match("((premium==true && age > 18) && name==\"Joe\") || job == \"ceo\""))
    }

}
