package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class SurveyDisplayQualifierTest {

    @Test
    fun `returns true if all matchers do so`() {
        val qualifier = SurveyDisplayQualifier.builder()
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .build()

        val survey = survey(id = 10)
        assertTrue(qualifier.doesQualify(survey))
    }

    @Test
    fun `return false if at least one matcher returns false`() {
        val qualifier = SurveyDisplayQualifier.builder()
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .register(mock {
                    on {matches(any())} doReturn false
                })
                .register(mock {
                    on {matches(any())} doReturn true
                })
                .build()

        val survey = survey(id = 10)
        assertFalse(qualifier.doesQualify(survey))
    }
}
