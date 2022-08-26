package com.qualaroo.util

import android.app.Activity
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertTrue

class SurveyTestUtil {

    companion object {

        fun <T : Activity> assertActivityFinishing(activityTestRule: ActivityTestRule<T>) {
            Thread.sleep(650)
            assertTrue(activityTestRule.activity.isFinishing)
        }

    }

}
