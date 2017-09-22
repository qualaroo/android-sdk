package com.qualaroo.internal

import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.util.InMemorySettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class SurveyDisplayQualifierTest {

    val localStorage = InMemoryLocalStorage(TimeProvider())
    val userPropertiesMatcher = AlwaysTrueUserPropertiesMatcher()
    val timeMatcher = AlwaysTrueTimeMatcher()

    val qualifier = SurveyDisplayQualifier(localStorage, userPropertiesMatcher, timeMatcher)

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

        var qualifier = SurveyDisplayQualifier(localStorage, userPropertiesMatcher, AlwaysFalseTimeMatcher())
        assertFalse(qualifier.shouldShowSurvey(survey))

        qualifier = SurveyDisplayQualifier(localStorage, userPropertiesMatcher, AlwaysTrueTimeMatcher())
        assertTrue(qualifier.shouldShowSurvey(survey))
    }

    @Test
    fun `should show only if custom matching is satisfied`() {
        val userInfo = UserInfo(InMemorySettings())
        val qualifier = SurveyDisplayQualifier(localStorage, UserPropertiesMatcher(userInfo), AlwaysTrueTimeMatcher())
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

    class AlwaysTrueTimeMatcher : TimeMatcher(null) {
        override fun enoughTimePassed(fromInMillis: Long): Boolean {
            return true
        }
    }

    class AlwaysFalseTimeMatcher : TimeMatcher(null) {
        override fun enoughTimePassed(fromInMillis: Long): Boolean {
            return false
        }
    }

    class AlwaysTrueUserPropertiesMatcher : UserPropertiesMatcher(null) {
        override fun match(customMap: String?): Boolean {
            return true
        }
    }
}