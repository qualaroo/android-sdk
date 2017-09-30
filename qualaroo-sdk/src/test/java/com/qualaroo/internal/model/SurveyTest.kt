package com.qualaroo.internal.model

import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyTest {

    @Test
    fun equals() {
        val survey = survey(
                id = 123
        )

        val differentInstanceOfASurvey = survey(
                id = 123
        )

        val differentSurvey = survey(
                id = 500
        )

        assertTrue(survey == survey)
        assertTrue(survey == differentInstanceOfASurvey)
        assertTrue(survey != differentSurvey)
    }

}