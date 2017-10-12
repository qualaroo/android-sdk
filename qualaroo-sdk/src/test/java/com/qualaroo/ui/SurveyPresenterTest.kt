package com.qualaroo.ui

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.qualaroo.internal.model.TestModels.answer
import com.qualaroo.internal.model.TestModels.message
import com.qualaroo.internal.model.TestModels.optionMap
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.ui.render.ThemeUtil.Companion.theme
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

@Suppress("IllegalIdentifier", "MemberVisibilityCanPrivate")
class SurveyPresenterTest {

    private val interactor = mock<SurveyInteractor>()
    private val presenter = SurveyPresenter(interactor, survey(id = 10), theme())

    val view = mock<SurveyView>()

    lateinit var capturedEventsObserver: SurveyInteractor.EventsObserver

    @Before
    fun setupDefaultPresenter() {
        presenter.setView(view)
        val captor = ArgumentCaptor.forClass(SurveyInteractor.EventsObserver::class.java)
        verify(interactor).registerObserver(captor.capture())
        capturedEventsObserver = captor.value
    }

    @Test
    fun `setups view`() {
        val theme = theme(
                textColor = 100,
                dimColor = 200,
                buttonDisabledColor = 300,
                backgroundColor = 400
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
        val presenter = SurveyPresenter(interactor, survey, theme)

        presenter.setView(view)

        val expectedSurveyViewModel = SurveyViewModel(
                100, 400, 300, 200, true, true
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
        val state: SurveyPresenter.State = SurveyPresenter.State(question(id = 1))
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

        verify(interactor, times(1)).stopSurvey()
    }

    @Test
    fun `passes answers to interactor`() {
        capturedEventsObserver.showQuestion(question(id = 10))

        presenter.onAnswered(answer(id = 20))
        verify(interactor, times(1)).questionAnswered(question(id = 10), listOf(answer(id = 20)))

        presenter.onAnswered(listOf(answer(id = 20), answer(id = 30)))
        verify(interactor, times(1)).questionAnswered(question(id = 10), listOf(answer(id = 20), answer(id = 30)))

        presenter.onAnsweredWithText("lorem ipsum")
        verify(interactor, times(1)).questionAnsweredWithText(question(id = 10), "lorem ipsum")
    }

    @Test
    fun `shows question`() {
        capturedEventsObserver.showQuestion(question(id = 10))

        verify(view, times(1)).showQuestion(question(id = 10))
    }

    @Test
    fun `shows message()`() {
        capturedEventsObserver.showMessage(message(id = 10))

        verify(view, times(1)).showMessage(message(id = 10))
    }

    @Test
    fun `closes survey`() {
        capturedEventsObserver.closeSurvey()

        verify(view, times(1)).closeSurvey()
    }

}
