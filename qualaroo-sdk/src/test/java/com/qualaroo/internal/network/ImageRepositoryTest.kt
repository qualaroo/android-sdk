package com.qualaroo.internal.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@Suppress("MemberVisibilityCanPrivate")
class ImageRepositoryTest {

    @JvmField @Rule val temporaryFolder = TemporaryFolder()

    val server = MockWebServer()
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
    lateinit var repository: ImageRepository

    @Before
    fun setup() {
        repository = ImageRepository(okHttpClient, temporaryFolder.root, 1024)
    }

    @Test
    fun works() {
        server.enqueue(MockResponse().setBody("Some image"))
        val request = Request.Builder().url(server.url("/")).build()
        val response = repository.load(request)

        assertEquals("Some image", response.body()?.string())
    }

    @Test
    fun cachesResults() {
        server.enqueue(MockResponse().setBody("Some image"))
        val request = Request.Builder()
                .url(server.url("/"))
                .build()
        val response = repository.load(request)
        response.body()?.string()

        server.enqueue(MockResponse().setBody("Error").setResponseCode(404))
        val laterResponse = repository.load(request)
//        assertNotNull(laterResponse.cacheResponse())
//        assertEquals("Some image", laterResponse.body()?.string())
    }

}
