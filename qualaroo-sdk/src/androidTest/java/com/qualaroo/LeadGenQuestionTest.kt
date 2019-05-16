package com.qualaroo

import android.support.design.widget.TextInputLayout
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.EditText
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.node
import com.qualaroo.internal.model.TestModels.qscreen
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.util.QualarooActivityTestRule
import com.qualaroo.util.SurveyTestUtil
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@Suppress("MemberVisibilityCanBePrivate")
@MediumTest
@RunWith(AndroidJUnit4::class)
class LeadGenQuestionTest {

    val survey = survey(
            id = 123,
            spec = spec(
                    startMap = mapOf(
                            language("en") to node(
                                    id = 1,
                                    nodeType = "qscreen"
                            )
                    ),
                    questionList = mapOf(
                            language("en") to listOf(
                                    question(
                                            id = 100,
                                            type = QuestionType.TEXT_SINGLE,
                                            cname = "first_name",
                                            title = "First name"
                                    ),
                                    question(
                                            id = 101,
                                            type = QuestionType.TEXT_SINGLE,
                                            cname = "last_name",
                                            title = "Last name"
                                    ),
                                    question(
                                            id = 102,
                                            type = QuestionType.TEXT_SINGLE,
                                            isRequired = true,
                                            cname = "phone",
                                            title = "Phone number"
                                    ),
                                    question(
                                            id = 103,
                                            type = QuestionType.TEXT_SINGLE,
                                            isRequired = true,
                                            cname = "email",
                                            title = "E-mail"
                                    )
                            )
                    ),
                    qscreenList = mapOf(
                            language("en") to listOf(
                                    qscreen(
                                            id = 1,
                                            description = "lala",
                                            questionList = listOf(100, 101, 102, 103),
                                            sendText = "I agree"
                                    )
                            )
                    )
            )
    )

    @Rule
    @JvmField
    val rule = QualarooActivityTestRule(survey)

    @Test
    fun rendersProperly() {
        onView(withId(R.id.qualaroo__view_question_lead_gen_input_fields)).check(matches(hasChildCount(4)))
        onView(hasTextInputLayoutHint("First name")).check(matches(isDisplayed()))
        onView(hasTextInputLayoutHint("Last name")).check(matches(isDisplayed()))
        onView(hasTextInputLayoutHint("Phone number *")).check(matches(isDisplayed()))
        onView(hasTextInputLayoutHint("E-mail *")).check(matches(isDisplayed()))

        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(withText("I agree")))
        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(not(isEnabled())))
    }

    @Test
    fun changesStateOfAButton() {
        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(not(isEnabled())))

        onEditTextInTextInputLayout(hasTextInputLayoutHint("First name")).perform(typeText("Mark"))
        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(not(isEnabled())))

        onEditTextInTextInputLayout(hasTextInputLayoutHint("Phone number *")).perform(typeText("123 123 123"))
        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(not(isEnabled())))

        onEditTextInTextInputLayout(hasTextInputLayoutHint("E-mail *")).perform(typeText("test@qualaroo.com"))
        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(isEnabled()))

        onEditTextInTextInputLayout(hasTextInputLayoutHint("Phone number *")).perform(clearText())
        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).check(matches(not(isEnabled())))
    }

    @Test
    fun closesAfterConfirm() {
        onEditTextInTextInputLayout(hasTextInputLayoutHint("Phone number *")).perform(typeText("123 123 123"))
        onView(withId(R.id.qualaroo__view_question_lead_gen_scroll_view)).perform(swipeUp())
        onEditTextInTextInputLayout(hasTextInputLayoutHint("E-mail *")).perform(typeText("test@qualaroo.com"))

        onView(withId(R.id.qualaroo__view_question_lead_gen_confirm)).perform(click())
        Thread.sleep(350) //todo: idling resources

        SurveyTestUtil.assertActivityFinishing(rule)
    }

    private fun onEditTextInTextInputLayout(matcher: Matcher<View>): ViewInteraction {
        return onView(allOf(isDescendantOfA(matcher), isAssignableFrom(EditText::class.java)))
    }

    private fun hasTextInputLayoutHint(hint: String) = object : TypeSafeMatcher<View>(TextInputLayout::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("has hint: $hint")
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false

            return hint == item.hint
        }
    }

}
