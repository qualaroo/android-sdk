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
class CheckboxQuestionTest {

    val checkboxQuestion = TestModels.question(
            id = 1,
            type = QuestionType.CHECKBOX,
            sendText = "go!",
            isRequired = false,
            answerList = listOf(
                    answer(1, "first"),
                    answer(2, "second"),
                    answer(3, "third"),
                    answer(4, "fourth")
            )
    )

    val requiredCheckboxQuestion = TestModels.question(
            id = 1,
            type = QuestionType.CHECKBOX,
            sendText = "go!",
            isRequired = true,
            answerList = listOf(
                    answer(1, "first"),
                    answer(2, "second"),
                    answer(3, "third"),
                    answer(4, "fourth")
            )
    )

    fun survey(checkboxQuestion: Question): Survey {
        return TestModels.survey(
                id = 1,
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        checkboxQuestion
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
    fun rendersCheckboxQuestion() {
        val rule = rule(checkboxQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(withText("go!")))

        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("first"))))
        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("second"))))
        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("third"))))
        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("fourth"))))

        rule.finishActivity()
    }

    @Test
    fun rendersRequiredCheckboxQuestion() {
        val rule = rule(requiredCheckboxQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(withText("go!")))

        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("first"))))
        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("second"))))
        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("third"))))
        onView(withId(R.id.qualaroo__view_question_checkbox_container)).check(matches(hasDescendant(withText("fourth"))))

        rule.finishActivity()
    }

    @Test
    fun unlocksButtonWhenAnyAnswerIsSelected() {
        val rule = rule(requiredCheckboxQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))

        onView(withText("first")).perform(click())
        onView(withText("second")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("second")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))

        rule.finishActivity()
    }

    @Test
    fun closesAfterConfirm() {
        val rule = rule(requiredCheckboxQuestion)
        rule.launchActivity(rule.activityIntent)

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).perform(click())

        SurveyTestUtil.assertActivityFinishing(rule)
    }

    @Test
    fun minAnswersCount_notRequired() {
        val question = TestModels.question(
                id = 1,
                type = QuestionType.CHECKBOX,
                sendText = "go!",
                isRequired = false,
                minAnswersCount = 2,
                answerList = listOf(
                        answer(1, "first"),
                        answer(2, "second"),
                        answer(3, "third"),
                        answer(4, "fourth")
                )
        )

        val rule = rule(question)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        rule.finishActivity()
    }

    @Test
    fun minAnswersCount_required() {
        val question = TestModels.question(
                id = 1,
                type = QuestionType.CHECKBOX,
                sendText = "go!",
                isRequired = true,
                minAnswersCount = 2,
                answerList = listOf(
                        answer(1, "first"),
                        answer(2, "second"),
                        answer(3, "third"),
                        answer(4, "fourth")
                )
        )

        val rule = rule(question)
        rule.launchActivity(rule.activityIntent)

        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))

        onView(withText("second")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))

        rule.finishActivity()
    }

    @Test
    fun maxAnswersCount() {
        val question = TestModels.question(
                id = 1,
                type = QuestionType.CHECKBOX,
                sendText = "go!",
                isRequired = false,
                maxAnswersCount = 2,
                answerList = listOf(
                        answer(1, "first"),
                        answer(2, "second"),
                        answer(3, "third"),
                        answer(4, "fourth")
                )
        )

        val rule = rule(question)
        rule.launchActivity(rule.activityIntent)

        onView(withText("first")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("second")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        onView(withText("third")).check(matches(not(isEnabled())))
        onView(withText("fourth")).check(matches(not(isEnabled())))

        rule.finishActivity()
    }


    @Test
    fun minSameAsMax() {
        val question = TestModels.question(
                id = 1,
                type = QuestionType.CHECKBOX,
                sendText = "go!",
                isRequired = true,
                minAnswersCount = 4,
                maxAnswersCount = 4,
                answerList = listOf(
                        answer(1, "first"),
                        answer(2, "second"),
                        answer(3, "third"),
                        answer(4, "fourth")
                )
        )

        val rule = rule(question)
        rule.launchActivity(rule.activityIntent)

        onView(withText("first")).perform(click())
        onView(withText("second")).perform(click())
        onView(withText("third")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(not(isEnabled())))

        onView(withText("fourth")).perform(click())
        onView(withId(R.id.qualaroo__view_question_checkbox_confirm)).check(matches(isEnabled()))

        rule.finishActivity()
    }


}
