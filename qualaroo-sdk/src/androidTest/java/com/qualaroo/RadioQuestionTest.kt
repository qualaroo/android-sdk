package com.qualaroo

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qualaroo.internal.model.Question
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.node
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.util.QualarooActivityTestRule
import com.qualaroo.util.SurveyTestUtil
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("MemberVisibilityCanBePrivate")
@MediumTest
@RunWith(AndroidJUnit4::class)
class RadioQuestionTest {

    val radioQuestion = TestModels.question(
            id = 1,
            type = QuestionType.RADIO,
            sendText = "go!",
            answerList = listOf(
                    answer(1, "first"),
                    answer(2, "second"),
                    answer(3, "third"),
                    answer(4, "fourth")
            )
    )

    val radioQuestionWithoutConfirmButton = TestModels.question(
            id = 1,
            type = QuestionType.RADIO,
            sendText = "go!",
            isRequired = true,
            answerList = listOf(
                    answer(1, "first"),
                    answer(2, "second"),
                    answer(3, "third"),
                    answer(4, "fourth")
            ),
            alwaysShowSend = false
    )

    fun survey(question: Question): Survey {
        return TestModels.survey(
                id = 1,
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        question
                                )
                        ),
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        )
                )
        )
    }

    fun rule(question: Question): QualarooActivityTestRule {
        return QualarooActivityTestRule(survey(question))
    }
    
    @Test
    fun rendersWithButton() {
        val rule = rule(radioQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("first"))))
        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("second"))))
        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("third"))))
        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("fourth"))))

        onView(withId(R.id.qualaroo__question_radio_confirm)).check(matches(withText("go!")))
        onView(withId(R.id.qualaroo__question_radio_confirm)).check(matches(isDisplayed()))

        rule.finishActivity()
    }
    
    @Test
    fun rendersWithoutButton() {
        val rule = rule(radioQuestionWithoutConfirmButton)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("first"))))
        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("second"))))
        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("third"))))
        onView(withId(R.id.qualaroo__question_radio_options)).check(matches(hasDescendant(withText("fourth"))))

        onView(withId(R.id.qualaroo__question_radio_confirm)).check(matches(not(isDisplayed())))
        
        rule.finishActivity()
    }

    @Test
    fun closesAfterConfirm() {
        val rule = rule(radioQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__question_radio_confirm)).perform(click())

        SurveyTestUtil.assertActivityFinishing(rule)
    }

    @Test
    fun closesWhenSelectingOptionAndButtonNotAvailable() {
        val rule = rule(radioQuestionWithoutConfirmButton)

        rule.launchActivity(rule.activityIntent)

        onView(withText("first")).perform(click())
        Thread.sleep(300)
        SurveyTestUtil.assertActivityFinishing(rule)
    }

}
