package com.qualaroo.internal

import okio.ByteString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset

@Suppress("IllegalIdentifier")
class CredentialsTest {

    companion object {

        private val PROPER_KEY = "MTIzNDU6NzUyYmQ5OGVjMjEyMTAwMDAwOTIwYjllMGYyZjMyNWY3ZTZhNDU1Yjo2Nzg5MA=="

        private fun base64(text: String): String {
            return ByteString.encodeString(text, Charset.forName("UTF-8")).base64()
        }
    }

    @Test
    fun `unpacks credentials`() {
        val credentials = Credentials(PROPER_KEY)

        assertEquals("12345", credentials.apiKey())
        assertEquals("752bd98ec212100000920b9e0f2f325f7e6a455b", credentials.apiSecret())
        assertEquals("67890", credentials.siteId())
    }

    @Test(expected = InvalidCredentialsException::class)
    fun `throw exception when too many parts`() {
        Credentials(base64("123:abcd:456:abd3"))
    }

    @Test(expected = InvalidCredentialsException::class)
    fun `throws exception when api key not numeric only`() {
        Credentials(base64("123a:abcd:456"))
    }

    @Test(expected = InvalidCredentialsException::class)
    fun `throws exception when site id not numeric only`() {
        Credentials(base64("123:abcd:456a"))
    }

    @Test(expected = InvalidCredentialsException::class)
    fun `throws when null api key`() {
        Credentials(null)
    }

    @Test(expected = InvalidCredentialsException::class)
    fun `throws when api key is not base64`() {
        Credentials("lala")
    }
}
