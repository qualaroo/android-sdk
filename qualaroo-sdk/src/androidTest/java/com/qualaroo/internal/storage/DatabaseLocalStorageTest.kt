package com.qualaroo.internal.storage

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SmallTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("MemberVisibilityCanPrivate")
@SmallTest
@RunWith(AndroidJUnit4::class)
class DatabaseLocalStorageTest {

    lateinit var localStorage: LocalStorage

    @Before
    fun setup() {
        InstrumentationRegistry.getContext().deleteDatabase(DatabaseLocalStorage.DB_NAME)
        localStorage = DatabaseLocalStorage(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun storeFailedReportRequests() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")
        localStorage.storeFailedReportRequest("http://url.com/3")
        val storedRequests = localStorage.getFailedReportRequests(3)

        assertEquals(3, storedRequests.size)
        assertTrue(storedRequests.contains("http://url.com/1"))
        assertTrue(storedRequests.contains("http://url.com/2"))
        assertTrue(storedRequests.contains("http://url.com/3"))
    }

    @Test
    fun removeFailedReportRequests() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")
        localStorage.storeFailedReportRequest("http://url.com/3")
        localStorage.removeReportRequest("http://url.com/2")
        val requests = localStorage.getFailedReportRequests(3)

        assertEquals(2, requests.size)
        assertFalse(requests.contains("http://url.com/2"))
    }

    @Test
    fun fetchSpecifiedNumberOfReportsOrAllAvailable() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")
        localStorage.storeFailedReportRequest("http://url.com/3")
        localStorage.storeFailedReportRequest("http://url.com/4")
        localStorage.storeFailedReportRequest("http://url.com/5")

        var requests = localStorage.getFailedReportRequests(3)
        assertEquals(3, requests.size)

        requests = localStorage.getFailedReportRequests(7)
        assertEquals(5, requests.size)
    }

    @Test
    fun failedRequestsCount() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")

        assertEquals(2, localStorage.failedRequestsCount)

        localStorage.storeFailedReportRequest("http://url.com/3")
        localStorage.storeFailedReportRequest("http://url.com/4")

        assertEquals(4, localStorage.failedRequestsCount)
    }

    @Test
    fun markSurveyAsSeen() {
        val survey = survey(id = 24)

        localStorage.markSurveyAsSeen(survey)
        val status = localStorage.getSurveyStatus(survey)

        assertTrue(status.hasBeenSeen())
    }

    @Test
    fun markSurveyAsFinished() {
        val survey = survey(id = 24)

        localStorage.markSurveyFinished(survey)
        val status = localStorage.getSurveyStatus(survey)

        assertTrue(status.hasBeenFinished())
    }

    @Test
    fun keepPreviousStatusData() {
        val survey = survey(id = 24)

        localStorage.markSurveyAsSeen(survey)
        var status = localStorage.getSurveyStatus(survey)
        assertTrue(status.hasBeenSeen())
        assertFalse(status.hasBeenFinished())

        localStorage.markSurveyFinished(survey)
        status = localStorage.getSurveyStatus(survey)
        assertTrue(status.hasBeenSeen())
        assertTrue(status.hasBeenFinished())
    }

    @Test
    fun storeUserProperties() {
        var properties = localStorage.userProperties
        assertFalse(properties.containsKey("someKey"))
        assertEquals(0, properties.size)

        localStorage.updateUserProperty("someKey", "someValue")
        properties = localStorage.userProperties
        assertEquals(1, properties.size)
        assertEquals("someValue", properties["someKey"])
    }

    @Test
    fun replacesValuesForKeys() {
        localStorage.updateUserProperty("someKey", "someValue")
        assertEquals("someValue", localStorage.userProperties["someKey"])

        localStorage.updateUserProperty("someKey", "otherValue")
        assertEquals("otherValue", localStorage.userProperties["someKey"])
    }

    @Test
    fun removesUserPropertyWhenValueIsNull() {
        localStorage.updateUserProperty("someKey", "someValue")
        assertTrue(localStorage.userProperties.containsKey("someKey"))

        localStorage.updateUserProperty("someKey", null)
        assertFalse(localStorage.userProperties.containsKey("someKey"))
    }

    @Test
    fun storesUsersGroupSurveyPercent() {
        localStorage.storeUserGroupPercent(survey(id = 1), 25)
        localStorage.storeUserGroupPercent(survey(id = 2), 40)
        localStorage.storeUserGroupPercent(survey(id = 3), 100)

        assertEquals(25, localStorage.getUserGroupPercent(survey(id = 1)))
        assertEquals(40, localStorage.getUserGroupPercent(survey(id = 2)))
        assertEquals(100, localStorage.getUserGroupPercent(survey(id = 3)))

        assertNull(localStorage.getUserGroupPercent(survey(id = 4)))
    }

    @Test
    fun storesAbTestGroupPercent() {
        val surveys = listOf(survey(id = 1), survey(id = 2), survey(id = 3))
        localStorage.storeAbTestGroupPercent(surveys, 25)

        assertEquals(25, localStorage.getAbTestGroupPercent(surveys))
        assertEquals(25, localStorage.getAbTestGroupPercent(surveys.reversed()))

        assertNull(localStorage.getAbTestGroupPercent(listOf(survey(id = 1), survey(id = 2))))
    }
}
