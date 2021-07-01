package com.qualaroo.ui;

import android.content.Context;
import androidx.annotation.RestrictTo;

import com.qualaroo.internal.DeviceTypeMatcher;
import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.SdkSession;
import com.qualaroo.internal.SurveySession;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.event.SurveyEventPublisher;
import com.qualaroo.internal.executor.ExecutorSet;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.ApiConfig;
import com.qualaroo.internal.network.ReportClient;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.ui.render.Renderer;
import com.qualaroo.ui.render.Theme;
import com.qualaroo.util.Shuffler;
import com.qualaroo.util.UriOpener;

import java.util.concurrent.Executor;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SurveyComponent {

    private final SurveyPresenter surveyPresenter;
    private final Renderer renderer;
    private final ImageProvider imageProvider;

    private SurveyComponent(Survey survey, LocalStorage localStorage, ReportManager reportManager, Language preferredLanguage, SurveyEventPublisher surveyEventPublisher, Executor backgroundExecutor, Shuffler shuffler, UriOpener uriOpener, ImageProvider imageProvider, Executor uiExecutor) {
        Theme theme = Theme.create(survey.spec().optionMap().colorThemeMap());
        SurveyInteractor surveyInteractor = new SurveyInteractor(survey, localStorage,  reportManager, preferredLanguage, shuffler, surveyEventPublisher, backgroundExecutor, uiExecutor);
        this.surveyPresenter = new SurveyPresenter(surveyInteractor, survey, theme, uriOpener);
        this.renderer = new Renderer(theme);
        this.imageProvider = imageProvider;
    }

    void inject(SurveyFragment surveyFragment) {
        surveyFragment.surveyPresenter = this.surveyPresenter;
        surveyFragment.renderer = this.renderer;
        surveyFragment.imageProvider = this.imageProvider;
    }

    public static class Factory {
        private final Context context;
        private final RestClient restClient;
        private final LocalStorage localStorage;
        private final UserInfo userInfo;
        private final ExecutorSet executorSet;
        private final UriOpener uriOpener;
        private final ImageProvider imageProvider;

        public Factory(Context context, RestClient restClient, LocalStorage localStorage, UserInfo userInfo, ExecutorSet executorSet, UriOpener uriOpener, ImageProvider imageProvider) {
            this.context = context.getApplicationContext();
            this.restClient = restClient;
            this.localStorage = localStorage;
            this.userInfo = userInfo;
            this.executorSet = executorSet;
            this.uriOpener = uriOpener;
            this.imageProvider = imageProvider;
        }

        public SurveyComponent create(Survey survey, Language preferredLanguage) {
            ApiConfig apiConfig = new ApiConfig();
            SdkSession sdkSession = new SdkSession(context, new DeviceTypeMatcher.AndroidDeviceTypeProvider(context));
            ReportClient reportClient = new ReportClient(restClient, apiConfig, localStorage, userInfo, new SurveySession(), sdkSession);
            ReportManager reportManager = new ReportManager(reportClient, executorSet.backgroundExecutor());
            SurveyEventPublisher surveyEventPublisher = new SurveyEventPublisher(context);
            Shuffler shuffler = new Shuffler();
            return new SurveyComponent(survey, localStorage, reportManager, preferredLanguage, surveyEventPublisher, executorSet.backgroundExecutor(), shuffler, uriOpener, imageProvider, executorSet.uiThreadExecutor());
        }
    }

}
