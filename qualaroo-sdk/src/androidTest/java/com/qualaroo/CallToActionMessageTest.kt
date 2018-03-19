package com.qualaroo

import android.content.Intent
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasAction
import android.support.test.espresso.intent.matcher.IntentMatchers.hasData
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
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

    @Test
    fun redirectsAndClosesAfterCtaClick() {
        Intents.init()

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

