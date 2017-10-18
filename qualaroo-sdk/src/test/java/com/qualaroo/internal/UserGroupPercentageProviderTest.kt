package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class UserGroupPercentageProviderTest {

    val localStorage = InMemoryLocalStorage()
    val random = mock<Random> {
        on { nextInt(100) } doReturn listOf(10,20,30,40,50,60)
    }
    val provider = UserGroupPercentageProvider(localStorage, random)

    @Test
    fun `generates and saves in local storage user group percentage per survey`() {
        val survey = survey(
                id = 1
        )

        val percent = provider.userGroupPercent(survey)
        assertEquals(percent, provider.userGroupPercent(survey))

        val otherProvider = UserGroupPercentageProvider(localStorage, random)
        assertEquals(percent, otherProvider.userGroupPercent(survey))
    }

    @Test
    fun `random number is generated for each survey`() {
        val survey = survey(
                id = 1
        )

        val otherSurvey = survey(
                id = 2
        )

        assertNotEquals(
                provider.userGroupPercent(survey),
                provider.userGroupPercent(otherSurvey)
        )
    }
}
