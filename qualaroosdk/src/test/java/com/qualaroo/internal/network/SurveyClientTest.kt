package com.qualaroo.internal.network

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.SessionInfo
import com.qualaroo.internal.UserInfo
import com.qualaroo.internal.model.Survey
import okhttp3.HttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.Executor

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class SurveyClientTest {

    val emptySuccessfulResult: Result<Array<Survey?>> = Result.of(emptyArray())
    val restClient = mock<RestClient> {
        on { get(any(), any<Class<Array<Survey?>>>() )} doReturn emptySuccessfulResult
    }
    val apiConfig = mock<ApiConfig> {
        on { qualarooApi() } doReturn HttpUrl.parse("https://api.qualaroo.com/api/v1.5")
    }
    val sessionInfo = mock<SessionInfo> {
        on { appName() } doReturn "com.qualaroo.test"
        on { androidVersion() } doReturn "27"
        on { deviceType() } doReturn "GF3210"
        on { sdkVersion() } doReturn "1.0.0"
    }
    val userInfo = mock<UserInfo> {
        on { deviceId } doReturn "abcd1"
    }
    val executor: Executor = Executor { r -> r.run() }
    val client = SurveyClient(restClient, apiConfig, sessionInfo, userInfo, executor)

    @Test
    fun `builds request properly`() {
        client.fetchSurveys(object : Callback<List<Survey>> {
            override fun onSuccess(result: List<Survey>?) {
                //ignore
            }

            override fun onFailure(exception: Exception?) {
                //ignore
            }
        })

        val captor = argumentCaptor<HttpUrl>()
        verify(restClient).get(captor.capture(), eq(Array<Survey>::class.java))

        val url = captor.lastValue
        assertEquals("https", url.scheme())
        assertEquals("api.qualaroo.com", url.host())
        assertEquals("/api/v1.5/surveys", url.encodedPath())
        assertEquals("1", url.queryParameter("spec"))
        assertEquals("1", url.queryParameter("no_superpack"))
        assertEquals("1.0.0", url.queryParameter("SDK_version"))
        assertEquals("27", url.queryParameter("android_version"))
        assertEquals("GF3210", url.queryParameter("device_type"))
        assertEquals("abcd1", url.queryParameter("device_ID"))
        assertEquals("com.qualaroo.test", url.queryParameter("client_app"))
    }

    @Test
    fun `calls success`() {
        whenever(restClient.get(any(), any<Class<Array<Survey?>>>())).thenReturn(emptySuccessfulResult)
        var onSuccessCalled = false
        val callback = object : Callback<List<Survey>> {
            override fun onSuccess(result: List<Survey>?) {
                onSuccessCalled = true
            }

            override fun onFailure(exception: Exception?) {
                throw IllegalStateException()
            }
        }

        client.fetchSurveys(callback)

        assertTrue(onSuccessCalled)
    }

    @Test
    fun `calls failure`() {
        whenever(restClient.get(any(), any<Class<Array<Survey?>>>())).thenReturn(Result.error(IOException()))

        var onFailureCalled = false
        val callback = object : Callback<List<Survey>> {
            override fun onSuccess(result: List<Survey>?) {
                throw IllegalStateException()
            }

            override fun onFailure(exception: Exception?) {
                onFailureCalled = true
            }
        }

        client.fetchSurveys(callback)

        assertTrue(onFailureCalled)
    }
}