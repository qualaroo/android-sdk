package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels
import com.qualaroo.internal.storage.InMemoryLocalStorage
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class SurveyStatusMatcherTest {

    val localStorage = InMemoryLocalStorage()
    val timeMatcher = mock<TimeMatcher>()
    val matcher = SurveyStatusMatcher(localStorage, timeMatcher)

    @Before
    fun setup() {
        whenever(timeMatcher.enoughTimePassedFrom(any())).thenReturn(true)
    }

    @Test
    fun `should always show on "persistent" flag`() {
        val persistentSurvey = TestModels.survey(
                id = 1,
                spec = TestModels.spec(
                        requireMap = TestModels.requireMap(
                                isPersistent = true
                        )
                )
        )
        Assert.assertTrue(matcher.matches(persistentSurvey))

        markSurveyAsSeen(persistentSurvey)
        Assert.assertTrue(matcher.matches(persistentSurvey))

        markSurveyAsFinished(persistentSurvey)
        Assert.assertTrue(matcher.matches(persistentSurvey))
    }

    @Test
    fun `should display only once on "one_shot" flag`() {
        var oneShotSurvey = TestModels.survey(
                id = 1,
                spec = TestModels.spec(
                        requireMap = TestModels.requireMap(
                                isOneShot = true
                        )
                )
        )

        Assert.assertTrue(matcher.matches(oneShotSurvey))

        markSurveyAsSeen(oneShotSurvey)
        Assert.assertFalse(matcher.matches(oneShotSurvey))

        markSurveyAsFinished(oneShotSurvey)
        Assert.assertFalse(matcher.matches(oneShotSurvey))

        oneShotSurvey = TestModels.survey(
                id = 2,
                spec = TestModels.spec(
                        requireMap = TestModels.requireMap(
                                isOneShot = true
                        )
                )
        )

        markSurveyAsFinished(oneShotSurvey)
        Assert.assertFalse(matcher.matches(oneShotSurvey))
    }

    @Test
    fun `should only show if time matching is satisfied`() {
        val survey = TestModels.survey(
                id = 1,
                spec = TestModels.spec(
                        requireMap = TestModels.requireMap(
                                isPersistent = true
                        )
                )
        )

        whenever(timeMatcher.enoughTimePassedFrom(any())).thenReturn(false)
        Assert.assertFalse(matcher.matches(survey))

        whenever(timeMatcher.enoughTimePassedFrom(any())).thenReturn(true)
        Assert.assertTrue(matcher.matches(survey))
    }

    fun markSurveyAsSeen(survey: Survey) {
        localStorage.markSurveyAsSeen(survey)
    }

    fun markSurveyAsFinished(survey: Survey) {
        localStorage.markSurveyFinished(survey)
    }

}
