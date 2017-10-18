package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class SamplePercentMatcherTest {

    val percentageProvider = mock<UserGroupPercentageProvider>()
    val matcher = SamplePercentMatcher(percentageProvider)

    @Test
    fun `returns true when no sample percent target set`() {
        val survey = survey(id = 10, spec = spec(
                requireMap = requireMap(
                        samplePercent = null
                )
        ))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(0)
        assertTrue(matcher.matches(survey))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(50)
        assertTrue(matcher.matches(survey))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(100)
        assertTrue(matcher.matches(survey))
    }

    @Test
    fun `returns true when random percent is below sample percent`() {
        val survey = survey(id = 10, spec = spec(
                requireMap = requireMap(
                        samplePercent = 50
                )
        ))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(100)
        assertFalse(matcher.matches(survey))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(51)
        assertFalse(matcher.matches(survey))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(50)
        assertFalse(matcher.matches(survey))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(49)
        assertTrue(matcher.matches(survey))

        whenever(percentageProvider.userGroupPercent(survey)).thenReturn(0)
        assertTrue(matcher.matches(survey))
    }

}
