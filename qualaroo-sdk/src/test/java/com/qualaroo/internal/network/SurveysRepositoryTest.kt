package com.qualaroo.internal.network

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.SessionInfo
import com.qualaroo.internal.UserInfo
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.util.TimeProvider
import okhttp3.HttpUrl
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class SurveysRepositoryTest {

    val restClient = mock<RestClient>()

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

    val timeProvider = mock<TimeProvider> {
        on { currentTimeMillis() } doReturn 0L
    }
    val timeLimit = 1000L

    val surveysRepository = SurveysRepository("abc123", restClient, apiConfig, sessionInfo, userInfo, Cache(timeProvider, timeLimit))

    @Test
    fun `calls proper request`() {
        restClientReturns(arrayOf(survey(id = 1), survey(id = 2), survey(id = 3)))
        surveysRepository.surveys

        val captor = argumentCaptor<HttpUrl>()
        verify(restClient).get(captor.capture(), eq(Array<Survey>::class.java))

        val url = captor.lastValue
        assertEquals("https", url.scheme())
        assertEquals("api.qualaroo.com", url.host())
        assertEquals("/api/v1.5/surveys", url.encodedPath())
        assertEquals("abc123", url.queryParameter("site_id"))
        assertEquals("1", url.queryParameter("spec"))
        assertEquals("1", url.queryParameter("no_superpack"))
        assertEquals("1.0.0", url.queryParameter("sdk_version"))
        assertEquals("27", url.queryParameter("os_version"))
        assertEquals("GF3210", url.queryParameter("device_type"))
        assertEquals("abcd1", url.queryParameter("device_id"))
        assertEquals("com.qualaroo.test", url.queryParameter("client_app"))
        assertEquals("Android", url.queryParameter("os"))
    }

    @Test
    fun `returns empty list when no surveys available`() {
        restClientReturnsError(IOException())

        val surveys = surveysRepository.surveys
        assertNotNull(surveys)
        assertEquals(0, surveys.size)
    }

    @Test
    fun `returns surveys from API when there were no previously stored surveys`() {
        restClientReturns(arrayOf(survey(id = 1), survey(id = 2), survey(id = 3)))
        val surveys = surveysRepository.surveys

        assertEquals(3, surveys.size)
        assertTrue(surveys.contains(survey(id = 1)))
        assertTrue(surveys.contains(survey(id = 2)))
        assertTrue(surveys.contains(survey(id = 3)))
    }

    @Test
    fun `caches results`() {
        restClientReturns(arrayOf(survey(id = 1), survey(id = 2), survey(id = 3)))

        var surveys = surveysRepository.surveys
        assertEquals(3, surveys.size)
        assertTrue(surveys.contains(survey(id = 1)))
        assertTrue(surveys.contains(survey(id = 2)))
        assertTrue(surveys.contains(survey(id = 3)))

        surveys = surveysRepository.surveys
        assertEquals(3, surveys.size)
        assertTrue(surveys.contains(survey(id = 1)))
        assertTrue(surveys.contains(survey(id = 2)))
        assertTrue(surveys.contains(survey(id = 3)))

        verify(restClient, times(1)).get(any(), eq(Array<Survey>::class.java))
    }

    @Test
    fun `filters out surveys that are not of "sdk" type`() {
        val surveys = arrayOf(
                survey(id = 1, type = "sdk"),
                survey(id = 2, type = "nps"),
                survey(id = 3, type = "definitely_not_sdk"),
                survey(id = 4, type = "sdk")
        )
        restClientReturns(surveys)

        val filteredSurveys = surveysRepository.surveys

        assertEquals(2, filteredSurveys.size)
        assertTrue(filteredSurveys.contains(survey(id = 1)))
        assertTrue(filteredSurveys.contains(survey(id = 4)))
    }

    @Test
    fun `filters out surveys that are not active`() {
        val surveys = arrayOf(
                survey(id = 1, active = true),
                survey(id = 2, active = true),
                survey(id = 3, active = false),
                survey(id = 4, active = true)
        )
        restClientReturns(surveys)

        val result = surveysRepository.surveys

        assertEquals(3, result.size)
        assertFalse(result.contains(survey(id = 3)))
    }

    @Test
    fun `filters out surveys that have questions of unknown type`() {
        val surveys = arrayOf(
                survey(id = 1, spec = spec(questionList = mapOf(
                        language("en") to listOf(
                                question(id = 1, type = QuestionType.UNKNOWN)
                        )
                ))),
                survey(id = 2, spec = spec(questionList = mapOf(
                        language("en") to listOf(
                                question(id = 1, type = QuestionType.RADIO)
                        )
                )))
        )
        restClientReturns(surveys)

        val result = surveysRepository.surveys

        assertEquals(1, result.size)
        assertFalse(result.contains(survey(id = 1)))
    }

    private fun restClientReturns(surveys: Array<Survey>) {
        whenever(restClient.get(any(), eq(Array<Survey>::class.java))).thenReturn(Result.of(surveys))
    }

    private fun restClientReturnsError(exception: Exception) {
        whenever(restClient.get(any(), eq(Array<Survey>::class.java))).thenReturn(Result.error(exception))
    }

}
