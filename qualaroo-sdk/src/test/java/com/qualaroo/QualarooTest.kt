package com.qualaroo

import com.nhaarman.mockito_kotlin.*
import com.qualaroo.internal.ImageProvider
import com.qualaroo.internal.SurveyDisplayQualifier
import com.qualaroo.internal.UserInfo
import com.qualaroo.internal.executor.ExecutorSet
import com.qualaroo.internal.model.TestModels.survey
import com.qualaroo.internal.network.RestClient
import com.qualaroo.internal.network.SurveysRepository
import com.qualaroo.internal.storage.InMemoryLocalStorage
import com.qualaroo.ui.SurveyComponent
import com.qualaroo.ui.SurveyStarter
import com.qualaroo.util.InMemorySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.Executor

@Suppress("IllegalIdentifier", "MemberVisibilityCanBePrivate")
class QualarooTest {

    val surveyComponentFactory = mock<SurveyComponent.Factory>()
    val surveysRepository = mock<SurveysRepository>()
    val surveyStarter = mock<SurveyStarter>()
    val surveyDisplayQualifier: SurveyDisplayQualifier = spy(SurveyDisplayQualifier.Builder().build())
    val userInfo = UserInfo(InMemorySettings(), InMemoryLocalStorage())
    val imageProvider = mock<ImageProvider>()
    val restClient = mock<RestClient>()
    val qualaroo = Qualaroo(
            surveyComponentFactory,
            surveysRepository,
            surveyStarter,
            surveyDisplayQualifier,
            userInfo,
            imageProvider,
            restClient,
            InMemoryLocalStorage(),
            CurrentThreadExecutorSet()
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

        verifyZeroInteractions(surveyStarter)
    }

    @Test
    fun `doesn't show survey if it's not qualified`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(survey(id = 1, canonicalName = "mySurvey")))
        whenever(surveyDisplayQualifier.shouldShowSurvey(survey(id = 1))).thenReturn(false)

        qualaroo.showSurvey("mySurvey")

        verifyZeroInteractions(surveyStarter)
    }

    @Test
    fun `ignores survey qualifier when targeting is disabled`() {
        whenever(surveysRepository.surveys).thenReturn(listOf(survey(id = 1, canonicalName = "mySurvey")))
        whenever(surveyDisplayQualifier.shouldShowSurvey(survey(id = 1))).thenReturn(false)

        val options = SurveyOptions.Builder()
                .ignoreSurveyTargeting(true)
                .build()
        qualaroo.showSurvey("mySurvey", options)

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

    private class CurrentThreadExecutorSet : ExecutorSet(
            Executor { it.run() },
            Executor { it.run() },
            Executor { it.run() }
    )

}
