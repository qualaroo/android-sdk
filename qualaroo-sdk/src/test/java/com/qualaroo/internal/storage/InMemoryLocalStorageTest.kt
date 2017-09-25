package com.qualaroo.internal.storage

import com.qualaroo.internal.TimeProvider
import com.qualaroo.internal.model.TestModels.survey
import org.junit.Assert.*
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class InMemoryLocalStorageTest {

    val localStorage = InMemoryLocalStorage(TimeProvider())

    @Test
    fun `store failed report requests`() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")
        localStorage.storeFailedReportRequest("http://url.com/3")
        val storedRequests = localStorage.getFailedReportRequests(3)

        assertEquals(3, storedRequests.size)
        assertTrue(storedRequests.contains("http://url.com/1"))
        assertTrue(storedRequests.contains("http://url.com/2"))
        assertTrue(storedRequests.contains("http://url.com/3"))

        localStorage.removeReportRequest("http://url.com/2")
        val requests = localStorage.getFailedReportRequests(3)

        assertEquals(2, requests.size)
        assertFalse(requests.contains("http://url.com/2"))
    }

    @Test
    fun `fetch specified number of reports or all available`() {
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
    fun `mark survey as seen`() {
        val survey = survey(id = 24)

        localStorage.markSurveyAsSeen(survey)
        val status = localStorage.getSurveyStatus(survey)

        assertTrue(status.hasBeenSeen())
    }

    @Test
    fun `mark survey as finished`() {
        val survey = survey(id = 24)

        localStorage.markSurveyFinished(survey)
        val status = localStorage.getSurveyStatus(survey)

        assertTrue(status.hasBeenFinished())
    }

    @Test
    fun `keep previous status data`() {
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
}