package com.qualaroo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.node
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.util.QualarooActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DisplaySurveyTest {

    companion object {
        val TEST_SURVEY = survey(
                id = 1,
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 1,
                                                type = QuestionType.RADIO,
                                                answerList = listOf(
                                                        answer(id = 1, title = "Answer 1"),
                                                        answer(id = 1, title = "Answer 2"),
                                                        answer(id = 1, title = "Answer 3")
                                                ),
                                                sendText = "Go Next",
                                                nextMap = node(
                                                        id = 101,
                                                        nodeType = "message"
                                                )
                                        )
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(id = 101, description = "Congratulations!")
                                )
                        ),
                        startMap = mapOf(
                                language("en") to node(id = 1, nodeType = "question")
                        )
                )
        )
    }

    @Rule
    @JvmField
    val activityTestRule = QualarooActivityTestRule(TEST_SURVEY)

    @Test
    fun opensUpSurveyFragment() {
        onView(withId(R.id.qualaroo__fragment_survey_container)).check(matches(isDisplayed()))
    }

    @Test
    fun displaysFirstQuestionFromSurvey() {
        onView(withText("Answer 1")).perform(ViewActions.click())
        onView(withText("Go Next")).perform(ViewActions.click())

        onView(withText("Congratulations!")).check(matches(isDisplayed()))
    }
}
