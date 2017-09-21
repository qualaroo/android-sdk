package com.qualaroo.internal

import okio.ByteString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset

@Suppress("IllegalIdentifier")
class CredentialsTest {

    companion object {

        private val PROPER_KEY = "API_KEY_HERE"

        private fun base64(text: String): String {
            return ByteString.encodeString(text, Charset.forName("UTF-8")).base64()
        }
    }

    @Test
    fun `unpacks credentials`() {
        val credentials = Credentials(PROPER_KEY)

        assertEquals("39241", credentials.apiKey())
        assertEquals("752bd98ec21216303a92fb9e0f2f325f7e6a455b", credentials.apiSecret())
        assertEquals("64832", credentials.siteId())
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



}
