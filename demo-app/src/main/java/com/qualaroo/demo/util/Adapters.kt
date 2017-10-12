package com.qualaroo.demo.util

import android.text.Editable
import android.text.TextWatcher

open class TextWatcherAdapter : TextWatcher {
    override fun afterTextChanged(s: Editable) {
        //stub
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        //stub
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        //stub
    }
}
