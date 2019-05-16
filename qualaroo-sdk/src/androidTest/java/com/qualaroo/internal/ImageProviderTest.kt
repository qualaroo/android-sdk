package com.qualaroo.internal

import android.graphics.Bitmap
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.qualaroo.internal.network.ImageRepository
import com.qualaroo.util.TestExecutors
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.net.InetAddress
import javax.net.ssl.SSLSocketFactory


@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
@SmallTest
@RunWith(AndroidJUnit4::class)
class ImageProviderTest {

    companion object {
        const val TEST_FILE = "test_survey_logo.jpg"
    }

    val server = MockWebServer().apply {
        useHttps(getSSLSocketFactory(), false)
    }
    val bitmapListener = CapturingBitmapListener()
    val imageRepository = ImageRepository(
            OkHttpClient.Builder().build(),
            InstrumentationRegistry.getTargetContext().cacheDir
    )
    val imageProvider =
            ImageProvider(
                    InstrumentationRegistry.getTargetContext(),
                    imageRepository,
                    TestExecutors.currentThread(),
                    TestExecutors.currentThread()
            )

    @Before
    fun setup() {
        server.start()
    }

    @After
    fun cleanUp() {
        server.shutdown()
    }

    @Test
    fun works() {
        var buffer = Buffer()
        buffer = buffer.readFrom(getAsset(TEST_FILE))
        server.enqueue(MockResponse()
                .addHeader("Content-Type:image/jpeg")
                .setBody(buffer))

        imageProvider.getImage(server.url("/").toString(), bitmapListener)

        assertEquals(128, bitmapListener.capturedBitmap?.height)
        assertEquals(128, bitmapListener.capturedBitmap?.width)
    }

    @Test
    fun caches() {
        var buffer = Buffer()
        buffer = buffer.readFrom(getAsset(TEST_FILE))
        server.enqueue(MockResponse()
                .addHeader("Content-Type:image/jpeg")
                .setBody(buffer))

        server.enqueue(MockResponse()
                .setResponseCode(404)
                .setBody("image not found"))

        imageProvider.getImage(server.url("/").toString(), bitmapListener)
        imageProvider.getImage(server.url("/").toString(), bitmapListener)

        assertEquals(128, bitmapListener.capturedBitmap?.height)
        assertEquals(128, bitmapListener.capturedBitmap?.width)
    }

    @Test
    fun doesNotCrashWhenNoBitmapListenerProvided() {
        var buffer = Buffer()
        buffer = buffer.readFrom(getAsset(TEST_FILE))
        server.enqueue(MockResponse()
                .addHeader("Content-Type:image/jpeg")
                .setBody(buffer))

        imageProvider.getImage(server.url("/").toString(), null)
        imageProvider.getImage(server.url("/").toString(), null)
    }

    private fun getAsset(assetPath: String): InputStream {
        return InstrumentationRegistry.getTargetContext().assets.open(assetPath)
    }

    class CapturingBitmapListener : ImageProvider.OnBitmapLoadedListener {
        var capturedBitmap: Bitmap? = null

        override fun onBitmapReady(bitmap: Bitmap) {
            capturedBitmap = bitmap
        }
    }

    @Throws(Exception::class)
    private fun getSSLSocketFactory(): SSLSocketFactory {
        val heldCertificate = okhttp3.tls.HeldCertificate.Builder()
                .commonName("localhost")
                .addSubjectAlternativeName(InetAddress.getByName("localhost").canonicalHostName)
                .rsa2048()
                .build()

        val serverCertificates = okhttp3.tls.HandshakeCertificates.Builder()
                .heldCertificate(heldCertificate)
                .build()
        return serverCertificates.sslSocketFactory()

    }
}
