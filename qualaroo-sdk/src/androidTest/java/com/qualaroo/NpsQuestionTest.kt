package com.qualaroo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import android.view.View
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.TestModels
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.ui.NpsView
import com.qualaroo.util.QualarooActivityTestRule
import com.qualaroo.util.SurveyTestUtil
import org.hamcrest.Matcher
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
        onView(withId(R.id.qualaroo__nps_scores)).perform(selectNpsScore(4))
        onView(withId(R.id.qualaroo__nps_view_confirm)).check(matches(isEnabled()))

        onView(withId(R.id.qualaroo__nps_scores)).perform(selectNpsScore(1))
        onView(withId(R.id.qualaroo__nps_view_confirm)).check(matches(isEnabled()))
    }

    @Test
    fun closesAfterConfirm() {
        onView(withId(R.id.qualaroo__nps_scores)).perform(selectNpsScore(4))
        onView(withId(R.id.qualaroo__nps_view_confirm)).perform(ViewActions.click())

        SurveyTestUtil.assertActivityFinishing(testRule)
    }

    private fun selectNpsScore(selectedScore: Int) = object : ViewAction {
        override fun getDescription(): String {
            return "select nps score"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(NpsView::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            (view as NpsView).setScore(selectedScore)
        }
    }


}
