package com.qualaroo.ui

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.optionMap
import com.qualaroo.internal.model.TestModels.qscreen
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.model.UserResponse
import com.qualaroo.ui.render.ThemeUtil.Companion.theme
import com.qualaroo.util.UriOpener
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class SurveyPresenterTest {


    private val uriOpener = mock<UriOpener>()
    private val interactor = mock<SurveyInteractor>()
    private val presenter = SurveyPresenter(interactor, survey(id = 10), theme(), uriOpener)

    val view = mock<SurveyView>()

    val captor = ArgumentCaptor.forClass(SurveyInteractor.EventsObserver::class.java)
    lateinit var capturedEventsObserver: SurveyInteractor.EventsObserver

    @Before
    fun setupDefaultPresenter() {
        doNothing().whenever(interactor).registerObserver(captor.capture())
        presenter.setView(view)
        capturedEventsObserver = captor.value
    }

    @Test
    fun `setups view`() {
        val theme = theme(
                textColor = 100,
                dimColor = 200,
                backgroundColor = 400,
                uiNormal = 300,
                uiSelected = 200,
                dimOpacity = 0.5f
        )
        val survey = survey(
                id = 1,
                spec = spec(
                        optionMap = optionMap(
                                mandatory = true,
                                showFullScreen = true
                        )
                )
        )
        val presenter = SurveyPresenter(interactor, survey, theme, uriOpener)
        presenter.setView(view)

        val expectedSurveyViewModel = SurveyViewModel(
                100, 400, 300, 200, 200, 0.5f, true, true, null, progressBarPosition
        )

        verify(view, times(1)).setup(expectedSurveyViewModel)
    }

    @Test
    fun `registers for interactor events`() {
        verify(interactor, times(1)).registerObserver(any())
    }

    @Test
    fun `unregisters observers when dropping view`() {
        presenter.dropView()

        verify(interactor, times(1)).unregisterObserver()
    }

    @Test
    fun `animates view when no state available`() {
        val emptyState: SurveyPresenter.State? = null
        presenter.init(emptyState)

        verify(view, times(1)).showWithAnimation()
    }

    @Test
    fun `does not animate when restoring state`() {
        val state: SurveyPresenter.State = SurveyPresenter.State(true)
        presenter.init(state)

        verify(view, times(1)).showImmediately()
    }

    @Test
    fun `requests to display survey`() {
        presenter.init(null)

        verify(interactor, times(1)).displaySurvey()
    }

    @Test
    fun `requests stopping survey on close clicked`() {
        presenter.onCloseClicked()

        verify(interactor, times(1)).requestSurveyToStop()
    }

    @Test
    fun `passes answers to interactor`() {
        capturedEventsObserver.showQuestion(question(id = 10))

        presenter.onResponse(UserResponse.Builder(10).addChoiceAnswer(20).build())
        verify(interactor, times(1)).onResponse(UserResponse.Builder(10).addChoiceAnswer(20).build())

        presenter.onResponse(UserResponse.Builder(10).addChoiceAnswer(20).addChoiceAnswer(30).build())
        verify(interactor, times(1)).onResponse(UserResponse.Builder(10).addChoiceAnswer(20).addChoiceAnswer(30).build())

        presenter.onResponse(UserResponse.Builder(10).addTextAnswer("lorem ipsum").build())
        verify(interactor, times(1)).onResponse(UserResponse.Builder(10).addTextAnswer("lorem ipsum").build())
    }

    @Test
    fun `shows question`() {
        capturedEventsObserver.showQuestion(question(id = 10))

        verify(view, times(1)).showQuestion(question(id = 10))
    }

    @Test
    fun `shows message`() {
        capturedEventsObserver.showMessage(message(id = 10))

        verify(view, times(1)).showMessage(message(id = 10), false)

        capturedEventsObserver.showQuestion(question(id = 10))
        capturedEventsObserver.showMessage(message(id = 20))
        verify(view, times(1)).showMessage(message(id = 20), true)
    }

    @Test
    fun `shows lead gen`() {
        capturedEventsObserver.showLeadGen(qscreen(id = -123), listOf(question(id = 10), question(id = 20)))

        verify(view, times(1)).showLeadGen(qscreen(id = -123), listOf(question(id = 10), question(id = 20)))
    }

    @Test
    fun `shows keyboard when lead gen is not a first question in survey`() {
        capturedEventsObserver.showLeadGen(qscreen(id = -123), listOf(question(id = 10)))
        verify(view, never()).forceShowKeyboardWithDelay(any())

        capturedEventsObserver.showLeadGen(qscreen(id = -124), listOf(question(id = 10)))
        verify(view, times(1)).forceShowKeyboardWithDelay(any())
    }

    @Test
    fun `closes survey`() {
        capturedEventsObserver.closeSurvey()

        verify(view, times(1)).closeSurvey()
    }

    @Test
    fun `passes close requests to interactor`() {
        presenter.onCloseClicked()

        verify(interactor, times(1)).requestSurveyToStop()
    }

    @Test
    fun `returns current state`() {
        capturedEventsObserver.showQuestion(question(id = 10))

        val savedState = presenter.savedState
        assertTrue(savedState.isDisplayingQuestion)
    }

    @Test
    fun `opens uri with UriOpener instance`() {
        capturedEventsObserver.openUri("someFancyUri")

        verify(uriOpener, times(1)).openUri("someFancyUri")
    }

}
