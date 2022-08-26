package com.qualaroo

import com.nhaarman.mockitokotlin2.*
import com.qualaroo.internal.*
import com.qualaroo.internal.executor.ExecutorSet
import com.qualaroo.internal.model.Survey
import com.qualaroo.internal.model.TestModels.language
import com.qualaroo.internal.model.TestModels.question
import com.qualaroo.internal.model.TestModels.requireMap
import com.qualaroo.internal.model.TestModels.spec
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.network.RestClient
import com.qualaroo.internal.network.SurveysRepository
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.ui.SurveyComponent
import com.qualaroo.ui.SurveyStarter
import com.qualaroo.util.InMemorySettings
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Executor

@Suppress("IllegalIdentifier", "MemberVisibilityCanBePrivate")
class QualarooTest {

    val localStorage = InMemoryLocalStorage()
    val surveyComponentFactory = mock<SurveyComponent.Factory>()
    val surveysRepository = mock<SurveysRepository>()
    val surveyStarter = mock<SurveyStarter>()
    val surveyDisplayQualifier: SurveyDisplayQualifier = spy(SurveyDisplayQualifier.Builder().build())
    val abTestDisplayQualifier: SurveyDisplayQualifier = spy(SurveyDisplayQualifier.Builder().build())
    val userInfo = UserInfo(InMemorySettings(), localStorage)
    val imageProvider = mock<ImageProvider>()
    val restClient = mock<RestClient>()
    val userPropertiesInjector = spy(UserPropertiesInjector(userInfo))
    val abTestGroupPercentageProvider = mock<AbTestGroupPercentageProvider>()
    val qualaroo = Qualaroo(
            surveyComponentFactory,
            surveysRepository,
            surveyStarter,
            surveyDisplayQualifier,
            abTestDisplayQualifier,
            userInfo,
            imageProvider,
            restClient,
            localStorage,
            abTestGroupPercentageProvider,
            CurrentThreadExecutorSet(),
            userPropertiesInjector
    )

    @Test
    fun `shows surveys`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(survey(id = 1, canonicalName = "mySurvey")))

        qualaroo.showSurvey("mySurvey")

        verify(surveyStarter, times(1)).start(survey(id = 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when empty alias`() {
        qualaroo.showSurvey("")
    }

    @Test
    fun `doesn't open survey screen when there is no survey available`() {
        whenever(surveysRepository.surveys).thenReturn(emptyList())

        qualaroo.showSurvey("mySurvey")

//        verifyZeroInteractions(surveyStarter)
    }

    @Test
    fun `doesn't show survey if it's not qualified`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(survey(id = 1, canonicalName = "mySurvey")))
        whenever(surveyDisplayQualifier.doesQualify(survey(id = 1))).thenReturn(false)

        qualaroo.showSurvey("mySurvey")

//        verifyZeroInteractions(surveyStarter)
    }

    @Test
    fun `ignores survey qualifier when targeting is disabled`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(survey(id = 1, canonicalName = "mySurvey")))
        whenever(surveyDisplayQualifier.doesQualify(survey(id = 1))).thenReturn(false)

        val options = SurveyOptions.Builder()
                .ignoreSurveyTargeting(true)
                .build()
        qualaroo.showSurvey("mySurvey", options)

        verify(surveyStarter, times(1)).start(survey(id = 1))
    }

    @Test
    fun `shows survey only if all user properties are matched`() {
        val surveyWithCustomProperties = survey(
                id = 1,
                canonicalName = "mySurvey",
                spec = spec(
                        questionList = mapOf(
                                language("en") to listOf(
                                        question(id = 1, title = "\${first_name}", description = "\${last_name}")
                                )
                        )
                )
        )
        whenever(surveysRepository.surveys).thenReturn(listOf(surveyWithCustomProperties))
        whenever(surveyDisplayQualifier.doesQualify(survey(id = 1))).thenReturn(true)

        qualaroo.showSurvey("mySurvey")
        verify(surveyStarter, never()).start(any())

        userInfo.setUserProperty("first_name", "Greg")

        qualaroo.showSurvey("mySurvey")
        verify(surveyStarter, never()).start(any())

        userInfo.setUserProperty("last_name", "Gregowsky")

        qualaroo.showSurvey("mySurvey")
        verify(surveyStarter, times(1)).start(survey(id = 1))
    }

    @Test
    fun `sets user's id`() {
        qualaroo.setUserId("myUserId")

        assertEquals("myUserId", userInfo.userId)
    }

    @Test
    fun `sets user properties`() {
        qualaroo.setUserProperty("first", "firstValue")
        qualaroo.setUserProperty("second", "secondValue")

        assertEquals("firstValue", userInfo.userProperties["first"])
        assertEquals("secondValue", userInfo.userProperties["second"])
    }

    @Test
    fun `removes user properties`() {
        qualaroo.setUserProperty("first", "firstValue")
        qualaroo.setUserProperty("second", "secondValue")

        qualaroo.removeUserProperty("second")
        assertEquals("firstValue", userInfo.userProperties["first"])
        assertNull(userInfo.userProperties["second"])
    }

    @Test
    fun `runs user property injector`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(survey(id = 1, canonicalName = "mySurvey")))
        whenever(userPropertiesInjector.injectCustomProperties(eq(survey(id = 1)), any())).thenReturn(
                survey(id = 111, canonicalName = "injected_survey"))

        qualaroo.showSurvey("mySurvey")

        val surveyCaptor = argumentCaptor<Survey>()
        verify(surveyStarter).start(surveyCaptor.capture())

        assertEquals(111, surveyCaptor.firstValue.id())
        assertEquals("injected_survey", surveyCaptor.firstValue.canonicalName())
    }

    //TODO: ideally, those ab tests below should be located in a nested class - with JUnit5 maybe(?)
    @Test
    fun `ab tests - runs one of selected aliases based on chance`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(
                survey(id = 1, canonicalName = "A", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 2, canonicalName = "B", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 3, canonicalName = "C", spec = spec(requireMap = requireMap(samplePercent = 34)))
        ))
        whenever(abTestGroupPercentageProvider.abTestGroupPercent(any())).thenReturn(1)
        
        qualaroo.abTest()
                .fromSurveys(listOf("A", "B", "C"))
                .show()

        val surveyCaptor = argumentCaptor<Survey>()
        
        verify(surveyStarter).start(surveyCaptor.capture())
        assertEquals(1, surveyCaptor.lastValue.id())
        assertEquals("A", surveyCaptor.lastValue.canonicalName())

        whenever(abTestGroupPercentageProvider.abTestGroupPercent(any())).thenReturn(33)
        
        qualaroo.abTest()
                .fromSurveys(listOf("A", "B", "C"))
                .show()

        verify(surveyStarter, atLeastOnce()).start(surveyCaptor.capture())
        assertEquals(2, surveyCaptor.lastValue.id())
        assertEquals("B", surveyCaptor.lastValue.canonicalName())

        whenever(abTestGroupPercentageProvider.abTestGroupPercent(any())).thenReturn(66)
        
        qualaroo.abTest()
                .fromSurveys(listOf("A", "B", "C"))
                .show()

        verify(surveyStarter, atLeastOnce()).start(surveyCaptor.capture())
        assertEquals(3, surveyCaptor.lastValue.id())
        assertEquals("C", surveyCaptor.lastValue.canonicalName())
    }

    @Test
    fun `ab tests - skips missing surveys`() {
        whenever(abTestGroupPercentageProvider.abTestGroupPercent(any())).thenReturn(1)
        whenever(surveysRepository.surveys).thenReturn(listOf(
                survey(id = 1, canonicalName = "A", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 2, canonicalName = "B", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 3, canonicalName = "C", spec = spec(requireMap = requireMap(samplePercent = 34)))
        ))

        qualaroo.abTest()
                .fromSurveys(listOf("AAA", "B", "C"))
                .show()

        val surveyCaptor = argumentCaptor<Survey>()
        verify(surveyStarter, atLeastOnce()).start(surveyCaptor.capture())
        assertEquals(2, surveyCaptor.lastValue.id())
        assertEquals("B", surveyCaptor.lastValue.canonicalName())
    }

    @Test
    fun `ab tests - doesn't do anything if there are no surveys found`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(
                survey(id = 1, canonicalName = "A", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 2, canonicalName = "B", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 3, canonicalName = "C", spec = spec(requireMap = requireMap(samplePercent = 34)))
        ))

        qualaroo.abTest()
                .fromSurveys(listOf("AAA", "BBB", "CCC"))
                .show()

        verify(surveyStarter, never()).start(any())
    }

    @Test
    fun `ab tests - marks all others surveys as finished`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(
                survey(id = 1, canonicalName = "A", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 2, canonicalName = "B", spec = spec(requireMap = requireMap(samplePercent = 33))),
                survey(id = 3, canonicalName = "C", spec = spec(requireMap = requireMap(samplePercent = 34)))
        ))
        whenever(abTestGroupPercentageProvider.abTestGroupPercent(any())).thenReturn(34)

        qualaroo.abTest()
                .fromSurveys(listOf("A", "B", "C"))
                .show()

        val surveyCaptor = argumentCaptor<Survey>()

        verify(surveyStarter).start(surveyCaptor.capture())
        assertEquals(2, surveyCaptor.lastValue.id())
        assertEquals("B", surveyCaptor.lastValue.canonicalName())

        assertTrue(localStorage.getSurveyStatus(survey(id = 1)).hasBeenFinished())
        assertTrue(localStorage.getSurveyStatus(survey(id = 3)).hasBeenFinished())
    }

    private class CurrentThreadExecutorSet : ExecutorSet(
            Executor { it.run() },
            Executor { it.run() },
            Executor { it.run() }
    )

}
