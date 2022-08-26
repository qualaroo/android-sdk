package com.qualaroo.internal.model


import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SurveyStatusTest {

    @Test
    fun testDefaultValues() {

        val defaultStatus = SurveyStatus.emptyStatus(survey(id = 1))

        assertFalse(defaultStatus.hasBeenSeen())
        assertFalse(defaultStatus.hasBeenFinished())
        assertEquals(0, defaultStatus.seenAtInMillis())
        assertEquals(1, defaultStatus.surveyId())
    }
}
