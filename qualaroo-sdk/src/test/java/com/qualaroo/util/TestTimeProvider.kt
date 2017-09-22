package com.qualaroo.util

import com.qualaroo.internal.TimeProvider

class TestTimeProvider : TimeProvider() {

    private var timeInMillis: Long = 0L

    fun setCurrentTime(timeInMillis: Long) {
        this.timeInMillis = timeInMillis
    }

    override fun nowInMillis(): Long {
        return timeInMillis
    }
}
