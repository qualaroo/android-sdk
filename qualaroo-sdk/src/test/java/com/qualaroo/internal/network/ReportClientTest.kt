package com.qualaroo.internal.network

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.LocalStorage
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

    val client = ReportClient(restClient, apiConfig, localStorage)

    @Test
    fun `builds proper url for impressions`() {
        client.recordImpression(survey)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/c.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
    }

    @Test
    fun `builds proper url for nps answer`() {
        val question = question(id = 123456, type = QuestionType.NPS)
        val answer = answer(id = 10)

        client.recordAnswer(survey, question, listOf(answer))

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
        assertEquals("10", url.queryParameter("r[123456]"))
    }

    @Test
    fun `builds proper url for radio answer`() {
        val question = question(id = 123456, type = QuestionType.RADIO)
        val answer = answer(id = 10)

        client.recordAnswer(survey, question, listOf(answer))

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
        assertEquals(answer.id().toString(), url.queryParameter("r[123456]"))
    }

    @Test
    fun `builds proper url for checkbox answer`() {
        val question = question(
                id = 123456,
                type = QuestionType.CHECKBOX
        )

        val answers = listOf(
                answer(id = 10),
                answer(id = 20),
                answer(id = 30)
        )

        client.recordAnswer(survey, question, answers)

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
        assertEquals(3, url.queryParameterValues("r[123456]").size)

        assertEquals("10", url.queryParameterValues("r[123456]")[0])
        assertEquals("20", url.queryParameterValues("r[123456]")[1])
        assertEquals("30", url.queryParameterValues("r[123456]")[2])
    }

    @Test
    fun `builds proper url for text answer`() {
        val question = question(
                id = 123456,
                type = QuestionType.TEXT
        )

        client.recordTextAnswer(survey, question, "long answer with spaces")

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals("123", url.queryParameter("id"))
        assertEquals("long answer with spaces", url.queryParameter("r[123456][text]"))
    }

    @Test
    fun `builds proper url for lead gen answers`() {
        client.recordLeadGenAnswer(survey, mapOf(
                1L to "John",
                2L to "Doe",
                3L to "mail@mail.com",
                4L to "+1 123 123 123"
        ))

        val url = restClient.recentHttpUrl!!

        assertEquals("https", url.scheme())
        assertEquals("turbo.qualaroo.com", url.host())
        assertEquals("/r.js", url.encodedPath())
        assertEquals(survey.id().toString(), url.queryParameter("id"))
        assertEquals("John", url.queryParameter("r[1][text]"))
        assertEquals("Doe", url.queryParameter("r[2][text]"))
        assertEquals("mail@mail.com", url.queryParameter("r[3][text]"))
        assertEquals("+1 123 123 123", url.queryParameter("r[4][text]"))
    }

    @Test
    fun `stores requests on network errors`() {
        restClient.throwsIoException = true

        client.recordImpression(survey(id = 10))
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())

        client.recordAnswer(survey(id = 10), question(id = 1), listOf(answer(id = 4)))
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())

        client.recordTextAnswer(survey(id = 10), question(id = 1), "textAnswer")
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
    }

    @Test
    fun `stores failed requests`() {
        restClient.returnedResponseCode = 200

        client.recordImpression(survey(id = 10))
        client.recordAnswer(survey(id = 10), question(id = 1), listOf(answer(id = 4)))
        client.recordTextAnswer(survey(id = 10), question(id = 1), "textAnswer")

        verify(localStorage, times(0)).storeFailedReportRequest(any())


        restClient.returnedResponseCode = 400

        client.recordImpression(survey(id = 10))
        client.recordAnswer(survey(id = 10), question(id = 1), listOf(answer(id = 4)))
        client.recordTextAnswer(survey(id = 10), question(id = 1), "textAnswer")

        verify(localStorage, times(0)).storeFailedReportRequest(any())


        restClient.returnedResponseCode = 500

        client.recordImpression(survey(id = 10))
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
        client.recordAnswer(survey(id = 10), question(id = 1), listOf(answer(id = 4)))
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
        client.recordTextAnswer(survey(id = 10), question(id = 1), "textAnswer")
        verify(localStorage).storeFailedReportRequest(restClient.recentHttpUrl?.toString())
    }

}
