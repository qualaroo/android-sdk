package com.qualaroo.internal.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.qualaroo.internal.model.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class RestClientTest {

    val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Language::class.java, LanguageJsonDeserializer())
            .registerTypeAdapter(QuestionType::class.java, QuestionTypeDeserializer())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
    lateinit var mockWebServer: MockWebServer

    val restClient = RestClient(okHttpClient, gson)

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun `returns successful results for 200 status code`() {
        mockWebServer.enqueue(mockResponseWithText("{}"))

        val result = restClient.get(mockWebServer.url("/"), Object::class.java)

        assertTrue(result.isSuccessful)
    }

    @Test
    fun `returns non-200 status code responses as errors with HttpException`() {
        val response = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(response)

        val result = restClient.get(mockWebServer.url("/"), Object::class.java)

        assertFalse(result.isSuccessful)
        assertTrue(result.exception is HttpException)
        assertEquals(400, (result.exception as HttpException).httpCode())
    }

    @Test
    fun `works for survey requests`() {
        mockWebServer.enqueue(mockResponseWithFile("api_responses/surveys.json"))

        val result = restClient.get(mockWebServer.url("/"), Array<Survey>::class.java)

        assertTrue(result.isSuccessful)
        val data = result.data
        assertEquals(7, data.size)
        assertEquals(177158, data[0].id())
        assertEquals(10, data[0].spec().requireMap().samplePercent())
        assertNull(data[1].spec().requireMap().samplePercent())
    }

    @Test
    fun `handles json parse errors`() {
        mockWebServer.enqueue(mockResponseWithText("-1508855936375"))

        val result = restClient.get(mockWebServer.url("/"), Integer::class.java)

        assertFalse(result.isSuccessful)
        assertTrue(result.exception is JsonParseException)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun mockResponseWithFile(jsonFilePath: String): MockResponse {
        val buffer = Buffer()
        buffer.readFrom(javaClass.classLoader.getResource(jsonFilePath).openStream())
        val result = MockResponse()
        result.body = buffer
        return result
    }

    private fun mockResponseWithText(json: String): MockResponse {
        val result = MockResponse()
        result.body = Buffer().writeUtf8(json)
        return result
    }

}
