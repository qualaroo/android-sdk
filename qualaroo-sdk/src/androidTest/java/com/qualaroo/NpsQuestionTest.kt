package com.qualaroo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.TestModels
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.question
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
class NpsQuestionTest {

    companion object {
        val NPS_SURVEY = survey(
                id = 1,
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 1,
                                                type = QuestionType.NPS,
                                                npsMinLabel = "minLabel",
                                                npsMaxLabel = "maxLabel",
                                                sendText = "confirm",
                                                answerList = listOf(
                                                        answer(id = 0),
                                                        answer(id = 1),
                                                        answer(id = 2),
                                                        answer(id = 3),
                                                        answer(id = 4),
                                                        answer(id = 5),
                                                        answer(id = 6),
                                                        answer(id = 7),
                                                        answer(id = 8),
                                                        answer(id = 9),
                                                        answer(id = 10)
                                                )
                                        )
                                )
                        ),
                        startMap = mapOf(
                                language("en") to TestModels.node(id = 1, nodeType = "question")
                        )
                )
        )
    }

    @Rule
    @JvmField
    val testRule = QualarooActivityTestRule(NPS_SURVEY)

    @Test
    fun displayLabelsProperly() {
        onView(withId(R.id.qualaroo__nps_view_confirm)).check(matches(withText("confirm")))
        onView(withId(R.id.qualaroo__nps_view_min_label)).check(matches(withText("minLabel")))
        onView(withId(R.id.qualaroo__nps_view_max_label)).check(matches(withText("maxLabel")))
    }

    @Test
    fun enablesButtonAfterSelection() {
        onView(withId(R.id.qualaroo__nps_view_confirm)).check(matches(not(isEnabled())))
        onView(withText("4")).perform(click())
        onView(withId(R.id.qualaroo__nps_view_confirm)).check(matches(isEnabled()))

        onView(withText("1")).perform(click())
        onView(withId(R.id.qualaroo__nps_view_confirm)).check(matches(isEnabled()))
    }

    @Test
    fun closesAfterConfirm() {
        onView(withText("5")).perform(click())
        onView(withId(R.id.qualaroo__nps_view_confirm)).perform(ViewActions.click())

        SurveyTestUtil.assertActivityFinishing(testRule)
    }
}
