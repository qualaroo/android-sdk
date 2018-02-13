package com.qualaroo.ui

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.ReportManager
import com.qualaroo.internal.event.SurveyEvent
import com.qualaroo.internal.event.SurveyEventPublisher
import com.qualaroo.internal.model.*
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
import com.qualaroo.internal.storage.LocalStorage
import com.qualaroo.util.Shuffler
import com.qualaroo.util.TestExecutors
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor

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
    val shuffler = Shuffler()
    val surveyEventPublisher = mock<SurveyEventPublisher>()
    val interactor = interactor()

    private fun interactor(
            withSurvey: Survey = survey,
            withStorage: LocalStorage = localStorage,
            withReportManager: ReportManager = reportManager,
            withPreferredLanguage: Language = preferredLanguage,
            withShuffler: Shuffler = shuffler,
            withEventPublisher: SurveyEventPublisher = surveyEventPublisher,
            withBackgroundExecutor: Executor = backgroundExecutor,
            withUiExecutor: Executor = uiExecutor
    ): SurveyInteractor {
        return SurveyInteractor(withSurvey, withStorage, withReportManager, withPreferredLanguage, withShuffler, withEventPublisher, withBackgroundExecutor, withUiExecutor)
    }

    @Before
    fun setup() {
        interactor.registerObserver(observer)
    }

    @Test
    fun `should close survey when no start node available`() {
        val survey = survey(
                id = 123
        )
        val interactor = interactor(withSurvey = survey)
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
        val interactor = interactor(withPreferredLanguage = language("unknown_language"))
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
        val interactor = interactor(withSurvey = survey, withPreferredLanguage = language("unknown_language"))
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
        val interactor = interactor(withSurvey = survey, withPreferredLanguage = language("en"))
        interactor.registerObserver(observer)

        interactor.displaySurvey()
        verify(observer).showQuestion(question(id = 100))
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(1).build())
        verify(observer).showQuestion(question(id = 200))
        interactor.onResponse(UserResponse.Builder(200).addChoiceAnswer(1).build())
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

        val interactor = interactor(withSurvey = survey)
        interactor.registerObserver(observer)

        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(1).build())
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
        val interactor = interactor(withSurvey = survey)
        interactor.registerObserver(observer)

        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(1).build())
        verify(observer).showQuestion(question(id = 200))
    }

    @Test
    fun `reports text answers`() {
        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).addTextAnswer("This is my answer").build())

        verify(reportManager, times(1)).reportUserResponse(survey, UserResponse.Builder(100).addTextAnswer("This is my answer").build())

    }

    @Test
    fun `reports answers`() {
        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(123).build())

        verify(reportManager, times(1))
                .reportUserResponse(survey, UserResponse.Builder(100).addChoiceAnswer(123).build())

        interactor.onResponse(
                UserResponse.Builder(100)
                        .addChoiceAnswer(1010)
                        .addChoiceAnswer(1011)
                        .build()
        )

        verify(reportManager, times(1)).reportUserResponse(
                survey,
                UserResponse.Builder(100)
                        .addChoiceAnswer(1010)
                        .addChoiceAnswer(1011)
                        .build()
        )
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
        val interactor = interactor(withSurvey = survey)
        interactor.displaySurvey()

        interactor.onLeadGenResponse(
                listOf(
                        UserResponse.Builder(100).addTextAnswer("John").build(),
                        UserResponse.Builder(101).addTextAnswer("Doe").build(),
                        UserResponse.Builder(102).addTextAnswer("+1 123 123 123").build()
                )
        )

        verify(reportManager).reportUserResponse(survey,
                listOf(
                        UserResponse.Builder(100).addTextAnswer("John").build(),
                        UserResponse.Builder(101).addTextAnswer("Doe").build(),
                        UserResponse.Builder(102).addTextAnswer("+1 123 123 123").build()
                )
        )
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
        val interactor = interactor(withSurvey = survey)
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        interactor.onLeadGenResponse(
                listOf(
                        UserResponse.Builder(100).addTextAnswer("John").build(),
                        UserResponse.Builder(101).addTextAnswer("Doe").build(),
                        UserResponse.Builder(102).addTextAnswer("+1 123 123 123").build()
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
        val interactor = interactor(withSurvey = survey)
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
        val interactor = interactor(withSurvey = survey)
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

        val interactor = interactor(withSurvey = survey)
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

        val interactor = interactor(withSurvey = survey)
        interactor.registerObserver(observer)
        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(10).build())

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

        val interactor = interactor(withSurvey = survey)
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

        verify(reportManager, times(1)).reportImpression(survey)
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
        val interactor = interactor(withSurvey = survey)

        assertFalse(localStorage.getSurveyStatus(survey).hasBeenSeen())

        interactor.displaySurvey()
        assertTrue(localStorage.getSurveyStatus(survey).hasBeenSeen())

        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(1).build())
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(1).build())

        interactor.requestSurveyToStop()
        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).build())

        assertTrue(localStorage.getSurveyStatus(survey).hasBeenSeen())
    }

    @Test
    fun `marks survey as finished`() {
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

        var interactor = interactor(withSurvey = survey)

        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).addChoiceAnswer(1).build())
        interactor.onResponse(UserResponse.Builder(200).addChoiceAnswer(2).build())

        interactor.requestSurveyToStop()

        assertFalse(localStorage.getSurveyStatus(survey).hasBeenFinished())

        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(300).build())

        assertTrue(localStorage.getSurveyStatus(survey).hasBeenFinished())


        survey = survey(
                id = 321,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 100,
                                        nodeType = "message"
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(id = 100)
                                )
                        )
                )

        )
        interactor = interactor(withSurvey = survey)
        interactor.displaySurvey()
        interactor.messageConfirmed(message(id = 100))
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
        val interactor = interactor(withSurvey = survey)
        interactor.registerObserver(observer)

        interactor.requestSurveyToStop()

        verifyZeroInteractions(observer)
    }

    @Test
    fun `accepts only first stopSurvey request`() {
        interactor.displaySurvey()
        interactor.requestSurveyToStop()
        interactor.requestSurveyToStop()
        interactor.requestSurveyToStop()
        interactor.messageConfirmed(message(id = 1))
        interactor.messageConfirmed(message(id = 1))
        interactor.requestSurveyToStop()

        verify(observer, times(1)).closeSurvey()
    }

    @Test
    fun `opens uri for CTA messages`() {
        val surveyWithRegularMessage = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "message"
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(1, MessageType.REGULAR)
                                )
                        )
                )
        )
        var interactor = interactor(withSurvey = surveyWithRegularMessage)
        interactor.registerObserver(observer)
        interactor.displaySurvey()
        interactor.messageConfirmed(message(id = 1, type = MessageType.REGULAR))
        verify(observer, never()).openUri(any())

        val surveyWithCtaMessage = survey(
                id = 1,
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
                                                id = 1,
                                                type = MessageType.CALL_TO_ACTION,
                                                ctaMap = ctaMap(
                                                        text = "buttonText",
                                                        uri = "http://qualaroo.com"
                                                )
                                        )
                                )
                        )
                )
        )
        interactor = interactor(withSurvey = surveyWithCtaMessage)
        interactor.registerObserver(observer)
        interactor.displaySurvey()
        interactor.messageConfirmed(
                message(
                        id = 1,
                        type = MessageType.CALL_TO_ACTION,
                        ctaMap = ctaMap(
                                text = "buttonText",
                                uri = "http://qualaroo.com"
                        )
                )
        )

        verify(observer, times(1)).openUri("http://qualaroo.com")
    }

    @Test
    fun `handles disableRandom flag`() {
        val observer = CapturingObserver()
        var survey = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 1,
                                                disableRandom = false,
                                                answerList = listOf(
                                                        answer(id = 1), answer(id = 2), answer(id = 3), answer(id = 4)
                                                )
                                        )
                                )
                        )
                )
        )
        var interactor = interactor(withSurvey = survey, withShuffler = JustReverseShuffler())
        interactor.registerObserver(observer)
        interactor.displaySurvey()
        var question = observer.question!!

        assertEquals(4, question.answerList()[0].id())
        assertEquals(3, question.answerList()[1].id())
        assertEquals(2, question.answerList()[2].id())
        assertEquals(1, question.answerList()[3].id())

        survey = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 1,
                                                disableRandom = true,
                                                answerList = listOf(
                                                        answer(id = 1), answer(id = 2), answer(id = 3), answer(id = 4)
                                                )
                                        )
                                )
                        )
                )
        )
        interactor = interactor(withSurvey = survey, withShuffler = JustReverseShuffler())
        interactor.registerObserver(observer)
        interactor.displaySurvey()
        question = observer.question!!

        assertEquals(1, question.answerList()[0].id())
        assertEquals(2, question.answerList()[1].id())
        assertEquals(3, question.answerList()[2].id())
        assertEquals(4, question.answerList()[3].id())
    }

    @Test
    fun `handles anchorLast and anchorLastCount flags`() {
        val observer = CapturingObserver()
        var survey = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                    question(
                                            id = 1,
                                            disableRandom = false,
                                            anchorLast = false,
                                            anchorLastCount = 0,
                                            answerList = listOf(
                                                    answer(id = 1), answer(id = 2), answer(id = 3), answer(id = 4)
                                            )
                                    )
                                )
                        )
                )
        )
        var interactor = interactor(withSurvey = survey, withShuffler = JustReverseShuffler())
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        var question = observer.question!!
        assertEquals(4, question.answerList()[0].id())
        assertEquals(3, question.answerList()[1].id())
        assertEquals(2, question.answerList()[2].id())
        assertEquals(1, question.answerList()[3].id())

        survey = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 1,
                                                disableRandom = false,
                                                anchorLast = true,
                                                anchorLastCount = 0,
                                                answerList = listOf(
                                                        answer(id = 1), answer(id = 2), answer(id = 3), answer(id = 4)
                                                )
                                        )
                                )
                        )
                )
        )
        interactor = interactor(withSurvey = survey, withShuffler = JustReverseShuffler())
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        question = observer.question!!
        assertEquals(3, question.answerList()[0].id())
        assertEquals(2, question.answerList()[1].id())
        assertEquals(1, question.answerList()[2].id())
        assertEquals(4, question.answerList()[3].id())

        survey = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(
                                                id = 1,
                                                disableRandom = false,
                                                anchorLast = true,
                                                anchorLastCount = 2,
                                                answerList = listOf(
                                                        answer(id = 1), answer(id = 2), answer(id = 3), answer(id = 4)
                                                )
                                        )
                                )
                        )
                )
        )

        interactor = interactor(withSurvey = survey, withShuffler = JustReverseShuffler())
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        question = observer.question!!
        assertEquals(2, question.answerList()[0].id())
        assertEquals(1, question.answerList()[1].id())
        assertEquals(3, question.answerList()[2].id())
        assertEquals(4, question.answerList()[3].id())

    }

    @Test
    fun `notifies about survey being shown`() {
        interactor.displaySurvey()

        verify(surveyEventPublisher, times(1)).publish(SurveyEvent.shown(survey.canonicalName()))
    }

    @Test
    fun `notifies about survey being finished`() {
        interactor.displaySurvey()
        interactor.onResponse(UserResponse.Builder(100).build())
        interactor.onResponse(UserResponse.Builder(101).build())

        verify(surveyEventPublisher, times(1)).publish(SurveyEvent.finished(survey.canonicalName()))
    }

    @Test
    fun `notifies about survey being dismissed`() {
        interactor.displaySurvey()
        interactor.requestSurveyToStop()

        verify(surveyEventPublisher, times(1)).publish(SurveyEvent.dismissed(survey.canonicalName()))
    }


    @Test
    fun `marks survey as finished when user dismisses it on last step that is also a message`() {
        val surveyWithLastNodeAsQuestion = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 1, nextMap = node(id = 2, nodeType = "question")),
                                        question(id = 2)
                                )
                        )
                )
        )
        var interactor = interactor(withSurvey = surveyWithLastNodeAsQuestion, withShuffler = JustReverseShuffler())
        interactor.onResponse(UserResponse.Builder(1).build())
        interactor.requestSurveyToStop()

        assertFalse(localStorage.getSurveyStatus(surveyWithLastNodeAsQuestion).hasBeenFinished())

        val surveyWithLastNodeAsMessage = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 1,
                                        nodeType = "question"
                                )
                        ),
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 1, nextMap = node(id = 2, nodeType = "message"))
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                    message(id = 2)
                                )
                        )
                )
        )

        interactor = interactor(withSurvey = surveyWithLastNodeAsMessage, withShuffler = JustReverseShuffler())
        interactor.onResponse(UserResponse.Builder(1).build())
        interactor.requestSurveyToStop()

        assertTrue(localStorage.getSurveyStatus(surveyWithLastNodeAsQuestion).hasBeenFinished())
    }

    @Test
    fun `allow to close mandatory messages`() {
        val survey = survey(
                id = 1,
                spec = spec(
                        startMap = mapOf(
                                language("en") to node(
                                        id = 100,
                                        nodeType = "message"
                                )
                        ),
                        msgScreenList = mapOf(
                                language("en") to listOf(
                                        message(id = 100)
                                )
                        ),
                        optionMap = optionMap(
                                mandatory = true
                        )
                )
        )
        val interactor = interactor(withSurvey = survey)
        interactor.registerObserver(observer)
        interactor.displaySurvey()

        interactor.messageConfirmed(message(id = 100))

        verify(observer, times(1)).closeSurvey()
    }


    class JustReverseShuffler : Shuffler() {
        override fun shuffle(list: MutableList<*>?) {
            list?.reverse()
        }
    }

    class CapturingObserver: SurveyInteractor.EventsObserver {
        var question: Question? = null

        override fun showQuestion(question: Question?) {
            this.question = question
        }

        override fun showMessage(message: Message?) {
        }

        override fun showLeadGen(qscreen: QScreen?, questions: MutableList<Question>?) {
        }

        override fun openUri(stringUri: String) {
        }

        override fun closeSurvey() {
        }
    }
}
