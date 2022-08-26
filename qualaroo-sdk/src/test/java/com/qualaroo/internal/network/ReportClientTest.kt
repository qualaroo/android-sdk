package com.qualaroo.internal.network

import com.nhaarman.mockitokotlin2.*
import com.qualaroo.internal.SdkSession
import com.qualaroo.internal.SurveySession
import com.qualaroo.internal.UserInfo
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.model.UserResponse
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.internal.storage.LocalStorage
import com.qualaroo.util.InMemorySettings
import com.qualaroo.util.MockRestClient
import okhttp3.HttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class ReportClientTest {

    val survey: Survey = survey(id = 123)

    val restClient = MockRestClient()

    val apiConfig = mock<ApiConfig> {
        on { reportApi() } doReturn HttpUrl.parse("https://turbo.qualaroo.com")
    }

    val localStorage = mock<LocalStorage>()

    val userInfo = spy(UserInfo(InMemorySettings(), InMemoryLocalStorage()))

    val session = mock<SurveySession> {
        on { uuid() } doReturn "mocked_session_id"
    }

    val sdkSession = mock<SdkSession>()

    val client = ReportClient(restClient, apiConfig, localStorage, userInfo, session, sdkSession)

    @Test
    fun `builds proper url for impressions`() {
        client.reportImpression(survey)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/c.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
        assertEquals(session.uuid(), url.queryParameter("u"))
    }

    @Test
    fun `builds proper url single choice questions`() {
        val userResponse = UserResponse.Builder(123456)
                .addChoiceAnswer(10)
                .build()

        client.reportUserResponse(survey, userResponse)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals(session.uuid(), url.queryParameter("u"))
        assertEquals("123", url.queryParameter("id"))
        assertEquals("10", url.queryParameter("r[123456][]"))
    }

    @Test
    fun `builds proper url for multiple choice questions`() {
        val userResponse = UserResponse.Builder(123456)
                .addChoiceAnswer(10)
                .addChoiceAnswer(20)
                .addChoiceAnswer(30)
                .build()

        client.reportUserResponse(survey, userResponse)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
        assertEquals(session.uuid(), url.queryParameter("u"))
        assertEquals(3, url.queryParameterValues("r[123456][]").size)

        assertEquals("10", url.queryParameterValues("r[123456][]")[0])
        assertEquals("20", url.queryParameterValues("r[123456][]")[1])
        assertEquals("30", url.queryParameterValues("r[123456][]")[2])

    }

    @Test
    fun `builds proper url for choice questions with freeform comments`() {
        val userResponse = UserResponse.Builder(123456)
                .addChoiceAnswerWithComment(1, "something1")
                .addChoiceAnswerWithComment(2, "something2")
                .build()

        client.reportUserResponse(survey, userResponse)
        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals(session.uuid(), url.queryParameter("u"))
        assertEquals("123", url.queryParameter("id"))

        assertEquals("something1", url.queryParameter("re[123456][1]"))
        assertEquals("something2", url.queryParameter("re[123456][2]"))
    }

    @Test
    fun `builds proper url for text questions`() {
        val userResponse = UserResponse.Builder(123456)
                .addTextAnswer("long answer with spaces")
                .build()

        client.reportUserResponse(survey, userResponse)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals(session.uuid(), url.queryParameter("u"))
        assertEquals("123", url.queryParameter("id"))
        assertEquals("long answer with spaces", url.queryParameter("r[123456][text]"))
    }

    @Test
    fun `builds proper url for lead gens`() {
        val leadGenResponse = mutableListOf<UserResponse>()

        leadGenResponse.add(UserResponse.Builder(1L).addTextAnswer("John").build())
        leadGenResponse.add(UserResponse.Builder(2L).addTextAnswer("Doe").build())
        leadGenResponse.add(UserResponse.Builder(3L).addTextAnswer("mail@mail.com").build())
        leadGenResponse.add(UserResponse.Builder(4L).addTextAnswer("+1 123 123 123").build())

        client.reportUserResponse(survey, leadGenResponse)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals(session.uuid(), url.queryParameter("u"))
        assertEquals(survey.id().toString(), url.queryParameter("id"))
        assertEquals("John", url.queryParameter("r[1][text]"))
        assertEquals("Doe", url.queryParameter("r[2][text]"))
        assertEquals("mail@mail.com", url.queryParameter("r[3][text]"))
        assertEquals("+1 123 123 123", url.queryParameter("r[4][text]"))
    }

    @Test
    fun `stores requests on network errors`() {
        restClient.throwsIoException = true

        client.reportImpression(survey(id = 10))
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())

        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())

        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
    }

    @Test
    fun `stores failed requests`() {
        restClient.returnedResponseCode = 200

        client.reportImpression(survey(id = 10))
        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())

        verify(localStorage, times(0)).storeFailedReportRequest(any())


        restClient.returnedResponseCode = 400

        client.reportImpression(survey(id = 10))
        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())

        verify(localStorage, times(0)).storeFailedReportRequest(any())


        restClient.returnedResponseCode = 500

        client.reportImpression(survey(id = 10))
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
    }

    @Test
    fun `injects user id and anonymous user id params in response events`() {
        val urls = mutableListOf<HttpUrl>()

        whenever(userInfo.deviceId).thenReturn("abcd1234")

        val leadGenResponse = mutableListOf<UserResponse>()
        leadGenResponse.add(UserResponse.Builder(1L).addTextAnswer("John").build())
        leadGenResponse.add(UserResponse.Builder(2L).addTextAnswer("Doe").build())
        client.reportUserResponse(survey, leadGenResponse)

        urls.add(restClient.recentHttpUrl!!)
        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())
        urls.add(restClient.recentHttpUrl!!)
        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        urls.add(restClient.recentHttpUrl!!)

        urls.forEach {
            assertEquals(null, it.queryParameter("i"))
            assertEquals("abcd1234", it.queryParameter("au"))
        }
        urls.clear()

        userInfo.userId = "lala"

        client.reportUserResponse(survey, leadGenResponse)
        urls.add(restClient.recentHttpUrl!!)
        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())
        urls.add(restClient.recentHttpUrl!!)
        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        urls.add(restClient.recentHttpUrl!!)

        urls.forEach {
            assertEquals("lala", it.queryParameter("i"))
        }
    }

    @Test
    fun `injects user properties to answers`() {
        userInfo.setUserProperty("first_key", "first_value")
        userInfo.setUserProperty("second_key", "second_value")

        val urls = mutableListOf<HttpUrl>()
        client.reportUserResponse(survey, UserResponse.Builder(1).addTextAnswer("some answer").build())
        urls.add(restClient.recentHttpUrl!!)

        client.reportUserResponse(survey, UserResponse.Builder(1).addChoiceAnswer(4).build())
        urls.add(restClient.recentHttpUrl!!)

        client.reportUserResponse(survey, UserResponse.Builder(123456).addChoiceAnswerWithComment(1, "something1").build())
        urls.add(restClient.recentHttpUrl!!)

        urls.forEach {
            assertEquals("first_value", it.queryParameter("rp[first_key]"))
            assertEquals("second_value", it.queryParameter("rp[second_key]"))
        }
    }

}
