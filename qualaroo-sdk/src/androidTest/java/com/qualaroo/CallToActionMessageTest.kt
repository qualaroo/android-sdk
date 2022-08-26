package com.qualaroo

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qualaroo.internal.model.Language
import com.qualaroo.internal.model.MessageType
import com.qualaroo.internal.model.TestModels
import com.qualaroo.internal.model.TestModels.ctaMap
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.util.QualarooActivityTestRule
import com.qualaroo.util.SurveyTestUtil
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class CallToActionMessageTest {

    val CTA_MESSAGE = survey(
            id = 1,
            spec = spec(
                    msgScreenList = mapOf(
                            language("en") to listOf(
                                    message(
                                            id = 1,
                                            type = MessageType.CALL_TO_ACTION,
                                            description = "La la la description",
                                            ctaMap = ctaMap(
                                                    text = "call_to_action",
                                                    uri = "http://qualaroo.com"
                                            )
                                    )
                            )
                    ),
                    startMap = mapOf(
                            language("en") to TestModels.node(
                                    id = 1,
                                    nodeType = "message"
                            )
                    )
            )
    )

    @Rule
    @JvmField
    val testRule = QualarooActivityTestRule(CTA_MESSAGE)

    @Test
    fun rendersProperly() {
        val expectedText = CTA_MESSAGE.spec().msgScreenList()[Language("en")]?.get(0)?.description()
        onView(withId(R.id.qualaroo__view_message_text)).check(matches(withText(expectedText)))

        onView(withId(R.id.qualaroo__view_message_cta)).check(matches(isEnabled()))
    }

    //TODO: figure out why is this causing the following to happen:
    /*com.qualaroo.CallToActionMessageTest > redirectsAndClosesAfterCtaClick[NexusSAPI19(AVD) - 4.4.2] FAILED
    java.lang.RuntimeException: No activities found. Did you forget to launch the activity by calling getActivity() or startActivitySync or similar?
    at android.support.test.espresso.base.RootViewPicker.waitForAtLeastOneActivityToBeResumed(RootViewPicker.java:169)*/

    fun redirectsAndClosesAfterCtaClick() {
        Intents.init()
        val okIntentResult = ActivityResult(Activity.RESULT_OK, Intent())
        intending(anyIntent()).respondWith(okIntentResult)

        onView(withId(R.id.qualaroo__view_message_cta)).perform(click())
        intended(allOf(
                hasAction(equalTo(Intent.ACTION_VIEW)),
                hasData("http://qualaroo.com")
        ))
        Intents.release()
        Espresso.pressBackUnconditionally()

        SurveyTestUtil.assertActivityFinishing(testRule)
    }
}

