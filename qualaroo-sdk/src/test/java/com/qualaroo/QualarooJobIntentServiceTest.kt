package com.qualaroo

import android.content.Intent
import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.ImageProvider
import com.qualaroo.internal.model.TestModels.optionMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.network.RestClient
import com.qualaroo.internal.network.SurveysRepository
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.internal.storage.LocalStorage
import com.qualaroo.util.MockRestClient
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class QualarooJobIntentServiceTest {

    val localStorage = spy(InMemoryLocalStorage())
    val restClient = spy(MockRestClient())

    val uploadIntent = mock<Intent> {
        on { action } doReturn QualarooJobIntentService.ACTION_UPLOAD_REQUESTS
    }

    val imageProvider = mock<ImageProvider>()

    private val service = OverridableQualarooJobIntentService(localStorage, restClient)

    @Test
    fun `does nothing on unknown actions`() {
        val badIntent = mock<Intent> {
            on { action } doReturn "some_gibberish"
        }

        service.onHandleWork(badIntent)

        verifyZeroInteractions(localStorage)
        verifyZeroInteractions(restClient)
    }

    @Test
    fun `does nothing when there are no failed requests stored`() {
        service.onHandleWork(uploadIntent)

        verifyZeroInteractions(restClient)
    }

    @Test
    fun `uploads failed requests`() {
        localStorage.storeFailedReportRequest("http://google.com/")

        service.onHandleWork(uploadIntent)

        assertEquals("http://google.com/", restClient.recentHttpUrl.toString())
    }

    @Test
    fun `sends only 50 requests at once`() {
        for (i in 0..100) {
            localStorage.storeFailedReportRequest("http://google.com/$i")
        }
        service.onHandleWork(uploadIntent)

        verify(restClient, times(50)).get(any())
    }

    @Test
    fun `removes requests that should not be retried`() {
        localStorage.storeFailedReportRequest("http://google.com/")
        service.onHandleWork(uploadIntent)
        assertEquals(0, localStorage.failedRequestsCount)


        localStorage.storeFailedReportRequest("http://google.com/")
        restClient.returnedResponseCode = 400
        service.onHandleWork(uploadIntent)
        assertEquals(0, localStorage.failedRequestsCount)


        localStorage.storeFailedReportRequest("http://google.com/")
        restClient.returnedResponseCode = 500
        service.onHandleWork(uploadIntent)
        assertEquals(1, localStorage.failedRequestsCount)
        
        restClient.throwsIoException = true
        service.onHandleWork(uploadIntent)
        assertEquals(1, localStorage.failedRequestsCount)
    }

    @Test
    fun `prefetches surveys with their images`() {
        service.onHandleWork(uploadIntent)

        verify(imageProvider, times(1)).getImage(eq("someLogoUrl"), anyOrNull())
        verify(imageProvider, times(1)).getImage(eq("someOtherLogoUrl"), anyOrNull())
        verify(imageProvider, times(1)).getImage(eq(null), anyOrNull())
        verifyNoMoreInteractions(imageProvider)
    }

    private inner class OverridableQualarooJobIntentService(val localStorage: LocalStorage, val restClient: RestClient) : QualarooJobIntentService() {
        override fun getQualarooInstance() = object : QualarooBase() {
            override fun localStorage(): LocalStorage = localStorage

            override fun restClient(): RestClient = restClient

            override fun surveysRepository() = mock<SurveysRepository> {
                on {surveys} doReturn listOf(
                        survey(id = 1, spec = spec(optionMap = optionMap(logoUrl = "someLogoUrl"))),
                        survey(id = 2, spec = spec(optionMap = optionMap(logoUrl = "someOtherLogoUrl"))),
                        survey(id = 3, spec = spec(optionMap = optionMap(logoUrl = null)))
                )
            }

            override fun imageProvider(): ImageProvider = imageProvider
        }
    }
}
