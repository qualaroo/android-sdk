package com.qualaroo.internal

import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@SuppressWarnings("IllegalIdentifier", "MemberVisibilityCanBePrivate")
class ActiveStatusMatcherTest {

    val matcher = ActiveStatusMatcher()

    @Test
    fun matches() {
        val activeSurvey = survey(id = 1, active = true)
        assertTrue(matcher.matches(activeSurvey))

        val inactiveSurvey = survey(id = 1, active = false)
        assertFalse(matcher.matches(inactiveSurvey))
    }

}
