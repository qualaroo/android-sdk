package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.util.InMemorySettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class SurveyDisplayQualifierTest {

    val localStorage = InMemoryLocalStorage(TimeProvider())
    private val userPropertiesMatcher = UserPropertiesMatcher(UserInfo(InMemorySettings()))
    private val timeMatcher = mock<TimeMatcher>()

    private val qualifier = SurveyDisplayQualifier(localStorage, userPropertiesMatcher, timeMatcher)

    @Before
    fun setup() {
        whenever(timeMatcher.enoughTimePassed(any())).thenReturn(true)
    }

    @Test
    fun `should always show on "persistent" flag`() {
        val persistentSurvey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                            isPersistent = true
                        )
                )
        )
        assertTrue(qualifier.shouldShowSurvey(persistentSurvey))

        markSurveyAsSeen(persistentSurvey)
        assertTrue(qualifier.shouldShowSurvey(persistentSurvey))

        markSurveyAsFinished(persistentSurvey)
        assertTrue(qualifier.shouldShowSurvey(persistentSurvey))
    }

    @Test
    fun `should display only once on "one_shot" flag`() {
        var oneShotSurvey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                isOneShot = true
                        )
                )
        )

        assertTrue(qualifier.shouldShowSurvey(oneShotSurvey))

        markSurveyAsSeen(oneShotSurvey)
        assertFalse(qualifier.shouldShowSurvey(oneShotSurvey))

        markSurveyAsFinished(oneShotSurvey)
        assertFalse(qualifier.shouldShowSurvey(oneShotSurvey))

        oneShotSurvey = survey(
                id = 2,
                spec = spec(
                        requireMap = requireMap(
                                isOneShot = true
                        )
                )
        )

        markSurveyAsFinished(oneShotSurvey)
        assertFalse(qualifier.shouldShowSurvey(oneShotSurvey))
    }

    @Test
    fun `should only show if time matching is satisfied`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                isPersistent = true
                        )
                )
        )

        whenever(timeMatcher.enoughTimePassed(any())).thenReturn(false)
        assertFalse(qualifier.shouldShowSurvey(survey))

        whenever(timeMatcher.enoughTimePassed(any())).thenReturn(true)
        assertTrue(qualifier.shouldShowSurvey(survey))
    }

    @Test
    fun `should show only if custom matching is satisfied`() {
        val userInfo = UserInfo(InMemorySettings())
        val qualifier = SurveyDisplayQualifier(localStorage, UserPropertiesMatcher(userInfo), timeMatcher)
        var survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                customMap = ""
                        )
                )
        )

        assertTrue(qualifier.shouldShowSurvey(survey))

        survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                customMap = "premium==true"
                        )
                )
        )
        assertFalse(qualifier.shouldShowSurvey(survey))

        userInfo.setUserProperty("premium", "true")
        assertTrue(qualifier.shouldShowSurvey(survey))
    }

    fun markSurveyAsSeen(survey: Survey) {
        localStorage.markSurveyAsSeen(survey)
    }

    fun markSurveyAsFinished(survey: Survey) {
        localStorage.markSurveyFinished(survey)
    }

}