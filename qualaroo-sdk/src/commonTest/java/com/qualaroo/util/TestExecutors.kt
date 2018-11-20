package com.qualaroo.util

import java.util.concurrent.Executor

class TestExecutors {

    companion object {
        fun currentThread() = CurrentThreadExecutor()
    }

    class CurrentThreadExecutor : Executor {
        override fun execute(command: Runnable) {
            command.run()
        }
    }
}
