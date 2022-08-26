package com.qualaroo.internal

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class UserIdentityMatcherTest {

    val userInfo = mock<UserInfo>()
    val matcher = UserIdentityMatcher(userInfo)

    @Test
    fun `matches every user when no targetting set`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                wantUserStr = null
                        )
                )
        )

        whenever(userInfo.userId).thenReturn(null)
        assertTrue(matcher.matches(survey))

        whenever(userInfo.userId).thenReturn("some_id")
        assertTrue(matcher.matches(survey))
    }

    @Test
    fun `matches any kind of user when "any" targeting type`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                wantUserStr = "any"
                        )
                )
        )

        whenever(userInfo.userId).thenReturn(null)
        assertTrue(matcher.matches(survey))

        whenever(userInfo.userId).thenReturn("some_id")
        assertTrue(matcher.matches(survey))
    }

    @Test
    fun `matches only identified users when known targetting type`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                wantUserStr = "yes"
                        )
                )
        )

        whenever(userInfo.userId).thenReturn(null)
        assertFalse(matcher.matches(survey))

        whenever(userInfo.userId).thenReturn("some_id")
        assertTrue(matcher.matches(survey))
    }

    @Test
    fun `matches only unidentified users when unknown targetting type`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                wantUserStr = "no"
                        )
                )
        )

        whenever(userInfo.userId).thenReturn(null)
        assertTrue(matcher.matches(survey))

        whenever(userInfo.userId).thenReturn("some_id")
        assertFalse(matcher.matches(survey))
    }

}
