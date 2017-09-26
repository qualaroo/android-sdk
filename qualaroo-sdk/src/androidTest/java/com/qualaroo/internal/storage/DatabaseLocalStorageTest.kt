package com.qualaroo.internal.storage

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
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

        Assert.assertEquals(3, storedRequests.size)
        Assert.assertTrue(storedRequests.contains("http://url.com/1"))
        Assert.assertTrue(storedRequests.contains("http://url.com/2"))
        Assert.assertTrue(storedRequests.contains("http://url.com/3"))
    }

    @Test
    fun removeFailedReportRequests() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")
        localStorage.storeFailedReportRequest("http://url.com/3")
        localStorage.removeReportRequest("http://url.com/2")
        val requests = localStorage.getFailedReportRequests(3)

        Assert.assertEquals(2, requests.size)
        Assert.assertFalse(requests.contains("http://url.com/2"))
    }

    @Test
    fun fetchSpecifiedNumberOfReportsOrAllAvailable() {
        localStorage.storeFailedReportRequest("http://url.com/1")
        localStorage.storeFailedReportRequest("http://url.com/2")
        localStorage.storeFailedReportRequest("http://url.com/3")
        localStorage.storeFailedReportRequest("http://url.com/4")
        localStorage.storeFailedReportRequest("http://url.com/5")

        var requests = localStorage.getFailedReportRequests(3)
        Assert.assertEquals(3, requests.size)

        requests = localStorage.getFailedReportRequests(7)
        Assert.assertEquals(5, requests.size)
    }
    
}