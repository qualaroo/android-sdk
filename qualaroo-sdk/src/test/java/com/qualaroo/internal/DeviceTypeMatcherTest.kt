package com.qualaroo.internal

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("IllegalIdentifier")
class DeviceTypeMatcherTest {

    private val deviceTypeProvider = mock<DeviceTypeMatcher.DeviceTypeProvider>()
    private val matcher = DeviceTypeMatcher(deviceTypeProvider)

    @Test
    fun `should not match when no device type specified`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                deviceTypeList = emptyList()
                        )
                )
        )
        assertFalse(matcher.doesDeviceMatch(survey))
    }

    @Test
    fun `should match if device type matches survey requirements`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        requireMap = requireMap(
                                deviceTypeList = listOf(
                                        "phone",
                                        "tablet"
                                )
                        )
                )
        )

        whenever(deviceTypeProvider.deviceType()).thenReturn("phone")
        assertTrue(matcher.doesDeviceMatch(survey))

        whenever(deviceTypeProvider.deviceType()).thenReturn("tablet")
        assertTrue(matcher.doesDeviceMatch(survey))

        whenever(deviceTypeProvider.deviceType()).thenReturn("desktop")
        assertFalse(matcher.doesDeviceMatch(survey))
    }

}
