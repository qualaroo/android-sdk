package com.qualaroo.util

import com.qualaroo.internal.storage.Settings
import java.util.*

class InMemorySettings : Settings(null) {

    private val map = HashMap<String, String>()

    override fun store(key: String, value: String) {
        map.put(key, value)
    }

    override fun get(key: String): String? {
        return map[key]
    }
}
