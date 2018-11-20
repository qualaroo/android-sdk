package com.qualaroo

import android.support.test.espresso.Espresso
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
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
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
@MediumTest
@RunWith(AndroidJUnit4::class)
class SurveyLanguageTest {

    val JUST_MESSAGE = survey(
            id = 1,
            spec = spec(
                    msgScreenList = mapOf(
                            language("en") to listOf(
                                    message(
                                            id = 1,
                                            type = MessageType.REGULAR,
                                            description = "English language"
                                    )
                            ),
                            language("pl") to listOf(
                                    message(
                                            id = 2,
                                            type = MessageType.REGULAR,
                                            description = "Polish language"
                                    )
                            ),
                            language("fi") to listOf(
                                    message(
                                            id = 3,
                                            type = MessageType.REGULAR,
                                            description = "Finnish language"
                                    )
                            ),
                            language("de") to listOf(
                                    message(
                                            id = 4,
                                            type = MessageType.REGULAR,
                                            description = "German language"
                                    )
                            )
                    ),
                    startMap = mapOf(
                            language("en") to node(
                                    id = 1,
                                    nodeType = "message"
                            ),
                            language("pl") to node(
                                    id = 2,
                                    nodeType = "message"
                            ),
                            language("fi") to node(
                                    id = 3,
                                    nodeType = "message"
                            ),
                            language("de") to node(
                                    id = 4,
                                    nodeType = "message"
                            )
                    ),
                    surveyVariations = listOf(language("en"), language("pl"), language("fi"), language("de"))
            )
    )


    @Test
    fun usesPreferredLanguage() {
        val testRule = QualarooActivityTestRule(JUST_MESSAGE)
        testRule.postQualarooInitialize = {
            Qualaroo.getInstance().setPreferredLanguage("pl")
        }
        testRule.launchActivity()

        val expectedText = JUST_MESSAGE.spec().msgScreenList()[Language("pl")]?.get(0)?.description()
        Espresso.onView(ViewMatchers.withId(R.id.qualaroo__view_message_text)).check(
                ViewAssertions.matches(ViewMatchers.withText(expectedText)))

        testRule.finishActivity()
    }

    @Test
    fun fallbacksToLocaleIfPreferredNotSet() {
        Locale.setDefault(Locale("fi"))
        val testRule = QualarooActivityTestRule(JUST_MESSAGE)
        testRule.launchActivity()

        val expectedText = JUST_MESSAGE.spec().msgScreenList()[Language("fi")]?.get(0)?.description()
        Espresso.onView(ViewMatchers.withId(R.id.qualaroo__view_message_text)).check(
                ViewAssertions.matches(ViewMatchers.withText(expectedText)))

        testRule.finishActivity()
    }

    @Test
    fun fallbacksToLocaleIfPreferredNotAvailable() {
        Locale.setDefault(Locale("fi"))
        val testRule = QualarooActivityTestRule(JUST_MESSAGE)
        testRule.postQualarooInitialize = {
            Qualaroo.getInstance().setPreferredLanguage("ru")
        }
        testRule.launchActivity()

        val expectedText = JUST_MESSAGE.spec().msgScreenList()[Language("fi")]?.get(0)?.description()
        Espresso.onView(ViewMatchers.withId(R.id.qualaroo__view_message_text)).check(
                ViewAssertions.matches(ViewMatchers.withText(expectedText)))

        testRule.finishActivity()
    }

    @Test
    fun fallbacksToFirstAvailableIfLocaleNotFoundAndPreferred() {
        Locale.setDefault(Locale("jp"))
        val testRule = QualarooActivityTestRule(JUST_MESSAGE)
        testRule.postQualarooInitialize = {
            Qualaroo.getInstance().setPreferredLanguage("ru")
        }
        testRule.launchActivity()

        val expectedText = JUST_MESSAGE.spec().msgScreenList()[Language("en")]?.get(0)?.description()
        Espresso.onView(ViewMatchers.withId(R.id.qualaroo__view_message_text)).check(
                ViewAssertions.matches(ViewMatchers.withText(expectedText)))

        testRule.finishActivity()
    }
}
