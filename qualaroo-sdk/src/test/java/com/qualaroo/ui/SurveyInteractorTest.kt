package com.qualaroo.ui

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.ReportManager
import com.qualaroo.internal.model.MessageType
import com.qualaroo.internal.model.QuestionType
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.ctaMap
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.node
import com.qualaroo.internal.model.TestModels.optionMap
import com.qualaroo.internal.model.TestModels.qscreen
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.util.TestExecutors
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
                                            answerList = listOf(answer(id = 123), answer(id = 124)),
                                            nextMap = node(
                                                    id = 101,
                                                    nodeType = "question"
                                            )
                                    ),
                                    question(
                                            id = 101,
                                            answerList = listOf(answer(id = 1010), answer(id = 1011))
                                    )
                            ),
                            language("pl") to listOf(
                                    question(
                                            id = 200
                                    )
                            )
                    ),
                    msgScreenList = mapOf(),
                    surveyVariations = listOf(language("en"), language("pl"))
            )
    )
    val localStorage = InMemoryLocalStorage()
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

        interactor.displaySurvey()

        verify(observer).closeSurvey()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `runs with preffered language`() {
        interactor.displaySurvey()

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

        interactor.displaySurvey()

        val expectedQuestion = question(
                id = 100
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
                                                id = 100
                                        )
                                ),
                                language("pl") to listOf(
                                        question(
                                                id = 200
                                        )
                                )
                        ),
                        msgScreenList = mapOf(),
                        surveyVariations = listOf(language("us"), language("pl"))
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, language("unknown_language"), backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.displaySurvey()

        val expectedQuestion = question(
                id = 100
        )
        verify(observer, times(1)).showQuestion(expectedQuestion)
    }

    @Test
    fun `continues survey instead of restarting`() {
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
                                                nextMap = node(
                                                        id = 200,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 1))
                                        ),
                                        question(
                                                id = 200,
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 1))
                                        ),
                                        question(
                                                id = 300
                                        )
                                )
                        ),
                        msgScreenList = mapOf()
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, language("en"), backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.displaySurvey()
        verify(observer).showQuestion(question(id = 100))
        interactor.questionAnswered(listOf(answer(id = 1)))
        verify(observer).showQuestion(question(id = 200))
        interactor.questionAnswered(listOf(answer(id = 1)))
        verify(observer, atMost(1)).showQuestion(question(id = 300))

        //presenter got lost and calls displaySurvey() again when it's ready to display it
        interactor.displaySurvey()
        verify(observer, atMost(1)).showQuestion(question(id = 100))
        verify(observer, atMost(2)).showQuestion(question(id = 300))
    }

    @Test
    fun `shows next screen based on nextNode from provided answer`() {
        val survey = survey(
                id = 1,
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
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(
                                                        answer(
                                                                id = 1,
                                                                nextMap = node(
                                                                        id = 200,
                                                                        nodeType = "question"
                                                                )
                                                        ),
                                                        answer(
                                                                id = 2,
                                                                nextMap = node(
                                                                        id = 300,
                                                                        nodeType = "question"
                                                                )
                                                        )
                                                )
                                        ),
                                        question(
                                                id = 200,
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                )
                                        ),
                                        question(
                                                id = 300,
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                )
                                        )
                                )
                        )

                )
        )

        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.displaySurvey()
        interactor.questionAnswered(listOf(answer(id = 1)))
        verify(observer).showQuestion(question(id = 200))
    }

    @Test
    fun `uses nextNode from question when provided answers don't have any`() {
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
                                                 nextMap = node(
                                                        id = 200,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 101))
                                        ),
                                        question(
                                                id = 200,
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                )
                                        )
                                )
                        ),
                        msgScreenList = mapOf()
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.displaySurvey()
        interactor.questionAnswered(listOf(answer(id = 101)))
        verify(observer).showQuestion(question(id = 200))
    }

    @Test
    fun `reports text answers`() {
        interactor.displaySurvey()
        interactor.questionAnsweredWithText("This is my answer")

        verify(reportManager, times(1))
                .recordTextAnswer(survey, question(id = 100), "This is my answer")
    }

    @Test
    fun `reports answers`() {
        interactor.displaySurvey()
        interactor.questionAnswered(listOf(answer(id = 123)))

        verify(reportManager, times(1))
                .recordAnswer(
                        survey,
                        question(id = 100),
                        listOf(answer(id = 123)))

        interactor.questionAnswered(
                listOf(answer(id = 1010), answer(id = 1011)))

        verify(reportManager, times(1))
                .recordAnswer(
                        survey,
                        question(id = 101),
                        listOf(answer(id = 1010), answer(id = 1011)))
    }

    @Test
    fun `reports lead gen answers`() {
        val survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "qspec"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 100),
                                        question(id = 101),
                                        question(id = 102)
                                )
                        ),
                        qscreenList = mapOf(
                                language("en") to listOf(
                                        qscreen(
                                                id = 1,
                                                questionList = listOf(100, 101, 102)
                                        )
                                )
                        )
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.displaySurvey()

        interactor.leadGenAnswered(
                mapOf(
                        100L to "John",
                        101L to "Doe",
                        102L to "+1 123 123 123"
                )
        )

        verify(reportManager).recordLeadGenAnswer(survey, mapOf(
                100L to "John",
                101L to "Doe",
                102L to "+1 123 123 123"
        ))
    }

    @Test
    fun `follows node from lead gen screens`() {
        val survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "qspec"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 100),
                                        question(id = 101),
                                        question(id = 102)
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(id = 1)
                                )
                        ),
                        qscreenList = mapOf(
                                language("en") to listOf(
                                        qscreen(
                                                id = 1,
                                                questionList = listOf(100, 101, 102),
                                                nextMap = node(
                                                        id = 1,
                                                        nodeType = "message"
                                                )
                                        )
                                )
                        )
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        interactor.leadGenAnswered(
                mapOf(
                        100L to "John",
                        101L to "Doe",
                        102L to "+1 123 123 123"
                )
        )

        verify(observer).showMessage(message(id = 1))
    }

    @Test
    fun `can show screen with message nodeType`() {
        val survey = survey(
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
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        verify(observer, times(1)).showMessage(message(id = 1))
    }

    @Test
    fun `can show screens with question nodeType`() {
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
                                                id = 100
                                        )
                                )
                        )
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        verify(observer, times(1)).showQuestion(question(id = 100))
    }

    @Test
    fun `can show screens with qscreen nodeType`() {
        val survey = survey(
                id = 123,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = -123,
                                        nodeType = "qscreen"
                                )
                        ),
                        qscreenList = mapOf(
                                language("en") to listOf(
                                        qscreen(
                                                id = -123,
                                                questionList = listOf(
                                                        100, 101, 102
                                                )
                                        )
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 99),
                                        question(id = 100),
                                        question(id = 101),
                                        question(id = 102)
                                )
                        )
                ))

        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        verify(observer, times(1)).showLeadGen(qscreen(id = -123), listOf(question(id = 100), question(id = 101), question(id = 102)))
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
                                                answerList = listOf(answer(id = 10))
                                        )
                                )
                        )
                )
        )

        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)
        interactor.displaySurvey()
        interactor.questionAnswered(listOf(answer(id = 10)))

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
        interactor.displaySurvey()
        interactor.messageConfirmed(message(id = 100))

        verify(observer).closeSurvey()
    }

    @Test
    fun `records impressions when starting survey`() {
        interactor.displaySurvey()

        //user rotated a device maybe?
        interactor.displaySurvey()

        verify(reportManager, times(1)).recordImpression(survey)
    }

    @Test
    fun `marks survey as seen once`() {
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
                                                nextMap = node(
                                                        id = 200,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 1))
                                        ),
                                        question(
                                                id = 200,
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 1))
                                        ),
                                        question(
                                                id = 300
                                        )
                                )
                        )
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)

        assertFalse(localStorage.getSurveyStatus(survey).hasBeenSeen())

        interactor.displaySurvey()
        assertTrue(localStorage.getSurveyStatus(survey).hasBeenSeen())

        interactor.questionAnswered(listOf(answer(id = 1)))
        interactor.questionAnswered(listOf(answer(id = 1)))

        interactor.stopSurvey()
        interactor.displaySurvey()
        interactor.questionAnswered(emptyList())

        assertTrue(localStorage.getSurveyStatus(survey).hasBeenSeen())
    }

    @Test
    fun `marks survey as finished`() {
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
                                                nextMap = node(
                                                        id = 200,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 1))
                                        ),
                                        question(
                                                id = 200,
                                                nextMap = node(
                                                        id = 300,
                                                        nodeType = "question"
                                                ),
                                                answerList = listOf(answer(id = 2))
                                        ),
                                        question(
                                                id = 300
                                        )
                                )
                        )
                )
        )

        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)

        interactor.displaySurvey()
        interactor.questionAnswered(listOf(answer(id = 1)))
        interactor.questionAnswered(listOf(answer(id = 2)))

        interactor.stopSurvey()

        assertFalse(localStorage.getSurveyStatus(survey).hasBeenFinished())

        interactor.displaySurvey()
        interactor.questionAnswered(emptyList())

        assertTrue(localStorage.getSurveyStatus(survey).hasBeenFinished())
    }

    @Test
    fun `ignores stopSurvey requests when survey is mandatory`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        optionMap = optionMap(
                            mandatory = true
                        )
                )
        )
        val interactor = SurveyInteractor(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor)
        interactor.registerObserver(observer)

        interactor.stopSurvey()

        verifyZeroInteractions(observer)
    }

    @Test
    fun `accepts only first stopSurvey request`() {
        interactor.stopSurvey()
        interactor.stopSurvey()
        interactor.stopSurvey()
        interactor.messageConfirmed(message(id = 1))
        interactor.messageConfirmed(message(id = 1))
        interactor.stopSurvey()

        verify(observer, times(1)).closeSurvey()
    }

    @Test
    fun `opens uri for CTA messages`() {
        interactor.messageConfirmed(message(id = 1, type = MessageType.REGULAR))
        verify(observer, never()).openUri(any())

        interactor.messageConfirmed(
                message(
                        id = 1,
                        type = MessageType.CALL_TO_ACTION,
                        description = "text",
                        ctaMap = ctaMap(
                                text = "buttonText",
                                uri = "http://qualaroo.com"
                        )
                )
        )

        verify(observer, times(1)).openUri("http://qualaroo.com")
    }

}
