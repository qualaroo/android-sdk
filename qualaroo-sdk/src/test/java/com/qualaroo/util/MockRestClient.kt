package com.qualaroo.util

import com.qualaroo.internal.network.RestClient
import okhttp3.HttpUrl
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MockRestClient : RestClient(null, null) {

    var returnedResponseCode = 200
    var throwsIoException = false
    var recentHttpUrl: HttpUrl? = null

    public override fun get(httpUrl: HttpUrl): Response {
        recentHttpUrl = httpUrl
        if (throwsIoException) {
            throw IOException()
        }
        return Response.Builder()
                .protocol(Protocol.HTTP_2)
                .message("")
                .request(Request.Builder().url(httpUrl).build())
                .code(returnedResponseCode)
                .build()
    }
}
