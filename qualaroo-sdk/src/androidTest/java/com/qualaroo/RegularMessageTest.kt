package com.qualaroo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import com.qualaroo.internal.model.Language
import com.qualaroo.internal.model.MessageType
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.node
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.util.QualarooActivityTestRule
import com.qualaroo.util.SurveyTestUtil
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class RegularMessageTest {

    val JUST_MESSAGE = survey(
            id = 1,
            spec = spec(
                    msgScreenList = mapOf(
                            language("en") to listOf(
                                    message(
                                            id = 1,
                                            type = MessageType.REGULAR,
                                            description = "La la la description"
                                    )
                            )
                    ),
                    startMap = mapOf(
                            language("en") to node(
                                    id = 1,
                                    nodeType = "message"
                            )
                    )
            )
    )

    @Rule
    @JvmField
    val testRule = QualarooActivityTestRule(JUST_MESSAGE)

    @Test
    fun rendersProperly() {
        val expectedText = JUST_MESSAGE.spec().msgScreenList()[Language("en")]?.get(0)?.description()
        onView(withId(R.id.qualaroo__view_message_text)).check(matches(withText(expectedText)))

        onView(withId(R.id.qualaroo__view_message_cta)).check(matches(isEnabled()))
        onView(withId(R.id.qualaroo__view_message_cta)).check(matches(not(isDisplayed())))
        onView(withId(R.id.qualaroo__survey_close)).check(matches(isDisplayed()))

    }

    @Test
    fun closesAfterConfirm() {
        onView(withId(R.id.qualaroo__survey_close)).perform(ViewActions.click())

        SurveyTestUtil.assertActivityFinishing(testRule)
    }

}

