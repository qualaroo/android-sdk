package com.qualaroo.util

import android.app.Activity
import android.support.test.rule.ActivityTestRule
import org.junit.Assert.assertTrue

class SurveyTestUtil {

    companion object {

        fun <T : Activity> assertActivityFinishing(activityTestRule: ActivityTestRule<T>) {
            Thread.sleep(650)
            assertTrue("Activity has finished", activityTestRule.activity.isFinishing)
        }

    }

}
