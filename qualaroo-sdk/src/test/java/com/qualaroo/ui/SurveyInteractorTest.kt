package com.qualaroo.ui

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.qualaroo.internal.ReportManager
import com.qualaroo.internal.TimeProvider
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.node
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.util.TestExecutors
import org.junit.Before
import org.junit.Test

@Suppress("MemberVisibilityCanPrivate", "IllegalIdentifier")
class SurveyInteractorTest {

    val survey = survey(
            id = 123,
            spec = spec(
                    startMap = mapOf(
                            language("en") to node(
                                    id = 100,
                                    nodeType = "question"
                            ),
                            language("pl") to node(
                                    id = 200,
                                    nodeType = "question"
                            )
                    ),
                    questionList = mapOf(
                            language("en") to listOf(
                                    question(
                                            id = 100,
                                            type = QuestionType.TEXT
                                    )
                            ),
                            language("pl") to listOf(
                                    question(
                                            id = 200,
                                            type = QuestionType.TEXT
                                    )
                            )
                    ),
                    msgScreenList = mapOf(),
                    surveyVariations = listOf(language("en"), language("pl"))
            )
    )
    val localStorage = InMemoryLocalStorage(TimeProvider())
    val reportManager = mock<ReportManager>()

    val backgroundExecutor = TestExecutors.currentThread()
    val uiExecutor = TestExecutors.currentThread()

    val preferredLanguage = language("en")
    val observer = mock<SurveyInteractor.EventsObserver>()
    val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)

    @Before
    fun setup() {
        interactor.registerObserver(observer)
    }

    @Test
    fun `should close survey when no start node available`() {
        val survey = survey(
                id = 123
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.startSurvey()

        verify(observer).closeSurvey()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `runs with preffered language`() {
        interactor.startSurvey()

        val expectedQuestion = question(
                id = 100,
                type = QuestionType.TEXT
        )
        verify(observer, times(1)).showQuestion(expectedQuestion)
    }

    @Test
    fun `fallbacks to "en" language when preferred one is not available`() {
        val interactor = SurveyInteractor(survey, localStorage, reportManager, language("unknown_language"), backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.startSurvey()

        val expectedQuestion = question(
                id = 100,
                type = QuestionType.TEXT
        )
        verify(observer, times(1)).showQuestion(expectedQuestion)
    }

    @Test
    fun `fallbacks to first language if no "en" language available`() {
        val survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("us") to node(
                                        id = 100,
                                        nodeType = "question"
                                ),
                                language("pl") to node(
                                        id = 200,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("us") to listOf(
                                        question(
                                                id = 100,
                                                type = QuestionType.TEXT
                                        )
                                ),
                                language("pl") to listOf(
                                        question(
                                                id = 200,
                                                type = QuestionType.TEXT
                                        )
                                )
                        ),
                        msgScreenList = mapOf(),
                        surveyVariations = listOf(language("us"), language("pl"))
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, language("unknown_language"), backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.startSurvey()

        val expectedQuestion = question(
                id = 100,
                type = QuestionType.TEXT
        )
        verify(observer, times(1)).showQuestion(expectedQuestion)
    }

    @Test
    fun `reports text answers`() {
        interactor.questionAnsweredWithText(question(id = 10, type = QuestionType.TEXT), "This is my answer")

        verify(reportManager, times(1))
                .recordTextAnswer(survey, question(id = 10, type = QuestionType.TEXT), "This is my answer")
    }

    @Test
    fun `reports answers`() {
        interactor.questionAnswered(question(id = 10, type = QuestionType.RADIO), listOf(answer(id = 123)))

        verify(reportManager, times(1))
                .recordAnswer(
                        survey,
                        question(id = 10, type = QuestionType.RADIO),
                        listOf(answer(id = 123)))

        interactor.questionAnswered(
                question(id = 10, type = QuestionType.RADIO),
                listOf(answer(id = 123), answer(id = 124)))

        verify(reportManager, times(1))
                .recordAnswer(
                        survey,
                        question(id = 10, type = QuestionType.RADIO),
                        listOf(answer(id = 123), answer(id = 124)))
    }

    @Test
    fun `can show objects based on nodeType field`() {
        var survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 100,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 100,
                                                type = QuestionType.TEXT
                                        )
                                )
                        )
                )
        )
        var interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.startSurvey()

        verify(observer, times(1)).showQuestion(question(id = 100, type = QuestionType.TEXT))

        survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "message"
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(
                                                id = 1
                                        )
                                )
                        )
                )
        )
        interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.startSurvey()

        verify(observer, times(1)).showMessage(message(id = 1))
    }

    @Test
    fun `closes survey on last question`() {
        val survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 100,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 100,
                                                type = QuestionType.RADIO,
                                                answerList = listOf(answer(id = 10))
                                        )
                                )
                        )
                )
        )

        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.startSurvey()
        interactor.questionAnswered(question(id = 100, type = QuestionType.RADIO), listOf(answer(id = 10)))

        verify(observer).closeSurvey()
    }

    @Test
    fun `closes survey on confirming message`() {
        val survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 100,
                                        nodeType = "message"
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(
                                                id = 100,
                                                description = "hello"
                                        )
                                )
                        )
                )
        )

        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.startSurvey()
        interactor.messageConfirmed(message(id = 100))

        verify(observer).closeSurvey()
    }

}