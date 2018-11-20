/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.demo.util

import java.io.BufferedReader
import java.io.InputStreamReader

class Logcat {
    companion object {

        @JvmStatic fun getLogs(): String {
            execSafely("logcat -d -v long QualarooSDK:V")
            val process = Runtime.getRuntime().exec("logcat -d -v long QualarooSDK:V")
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream))
            val log = StringBuilder()
            bufferedReader.lineSequence().forEach {
                log.append(it)
                log.append('\n')
            }
            return log.toString()
        }

        @JvmStatic fun clearLogcat() {
            try {
                execSafely("logcat -c")
            } catch (ignored: Exception) {}
        }

        private fun execSafely(command: String) {
            try {
                Runtime.getRuntime().exec(command)
            } catch (ignored: Exception) {}
        }
    }
}
