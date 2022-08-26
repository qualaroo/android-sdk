package com.qualaroo.internal

import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels
import com.qualaroo.internal.storage.InMemoryLocalStorage
import org.junit.Assert
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanBePrivate")
class SurveyStatusMatcherTest {

    val localStorage = InMemoryLocalStorage()
    val matcher = SurveyStatusMatcher(localStorage)

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

    fun markSurveyAsSeen(survey: Survey) {
        localStorage.markSurveyAsSeen(survey)
    }

    fun markSurveyAsFinished(survey: Survey) {
        localStorage.markSurveyFinished(survey)
    }

}
