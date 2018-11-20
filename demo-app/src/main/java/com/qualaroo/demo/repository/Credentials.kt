/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.demo.repository;

import okio.ByteString

class Credentials(apiKey: String) {

    val apiKey: String
    val apiSecret: String
    val siteId: String

    init {
        if (apiKey.isEmpty()) {
            throw IllegalArgumentException()
        }
        val byteKey = ByteString.decodeBase64(apiKey) ?: throw IllegalArgumentException()
        val key = byteKey.utf8()
        val keyParts = key.split(":".toRegex())
        if (keyParts.size != 3 || !isNumericOnly(keyParts[0]) || !isNumericOnly(keyParts[2])) {
            throw IllegalArgumentException()
        }
        this.apiKey = keyParts[0]
        this.apiSecret = keyParts[1]
        this.siteId = keyParts[2]
    }

    private fun isNumericOnly(text: String): Boolean {
        val chars = text.toCharArray()
        return chars.all { Character.isDigit(it) }
    }
}
