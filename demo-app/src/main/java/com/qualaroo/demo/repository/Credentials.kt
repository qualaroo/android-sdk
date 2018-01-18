package com.qualaroo.demo.repository;

import okio.ByteString

class Credentials(apiKey: String?) {

    private val apiKey: String
    private val apiSecret: String
    private val siteId: String

    init {
        if (apiKey == null || apiKey.isEmpty()) {
            throw IllegalArgumentException()
        }
        val byteKey = ByteString.decodeBase64(apiKey) ?: throw IllegalArgumentException()
        val key = byteKey.utf8()
        val keyParts = key.split(":".toRegex())
        if (keyParts.size != 3) {
            throw IllegalArgumentException()
        }
        if (!isNumericOnly(keyParts[0])) {
            throw IllegalArgumentException()
        }
        if (!isNumericOnly(keyParts[2])) {
            throw IllegalArgumentException()
        }
        this.apiKey = keyParts[0]
        this.apiSecret = keyParts[1]
        this.siteId = keyParts[2]
    }

    fun apiKey(): String {
        return apiKey
    }

    fun apiSecret(): String {
        return apiSecret
    }

    fun siteId(): String {
        return siteId
    }

    fun encodedApiKey(): String {
        return Base64.encode("$apiKey:$apiSecret:$siteId".toByteArray())
    }

    private fun isNumericOnly(text: String): Boolean {
        val chars = text.toCharArray()
        return chars.all { Character.isDigit(it) }
    }
}
