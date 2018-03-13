package com.qualaroo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
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
class TextQuestionTest {

    val textQuestion = TestModels.question(
            id = 1,
            type = QuestionType.TEXT,
            sendText = "go!",
            answerList = listOf(
                    answer(1, "first"),
                    answer(2, "second"),
                    answer(3, "third"),
                    answer(4, "fourth")
            )
    )

    val requiredTextQuestion = TestModels.question(
            id = 1,
            type = QuestionType.TEXT,
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
    fun renders() {
        val rule = rule(textQuestion)
        rule.launchActivity(rule.activityIntent)
        
        onView(withId(R.id.qualaroo__view_question_text_input)).check(matches(isDisplayed()))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(withText("go!")))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(isDisplayed()))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(isEnabled()))

        rule.finishActivity()
    }
    
    @Test
    fun rendersRequired() {
        val rule = rule(requiredTextQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_text_input)).check(matches(isDisplayed()))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(withText("go!")))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(isDisplayed()))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(not(isEnabled())))

        rule.finishActivity()
    }
    
    @Test
    fun changesButtonStateWhenRequired() {
        val rule = rule(requiredTextQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(not(isEnabled())))
        
        onView(withId(R.id.qualaroo__view_question_text_input)).perform(typeText("hello"))
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(isEnabled()))

        onView(withId(R.id.qualaroo__view_question_text_input)).perform(clearText())
        onView(withId(R.id.qualaroo__view_question_text_confirm)).check(matches(not(isEnabled())))

        rule.finishActivity()
    }

    @Test
    fun closesAfterConfirm() {
        val rule = rule(textQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_text_confirm)).perform(click())
        Thread.sleep(300)

        SurveyTestUtil.assertActivityFinishing(rule)
    }

}
