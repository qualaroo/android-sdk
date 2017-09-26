package com.qualaroo.ui;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.ui.render.Renderer;
import com.qualaroo.ui.render.Theme;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SurveyComponent {

    private final Survey survey;

    private SurveyInteractor surveyInteractor;
    private SurveyPresenter surveyPresenter;

    private Renderer renderer;
    private final Theme theme;

    public static SurveyComponent from(Survey survey) {
        return new SurveyComponent(survey);
    }

    private SurveyComponent(Survey survey) {
        this.survey = survey;
        this.theme = Theme.from(survey.spec().optionMap().colorThemeMap());
    }

    void inject(SurveyFragment surveyFragment) {
        surveyFragment.surveyPresenter = provideSurveyPresenter();
        surveyFragment.renderer = provideRenderer();
        surveyFragment.theme = theme;
    }

    private Renderer provideRenderer() {
        if (renderer == null) {
            renderer = new Renderer(theme);
        }
        return renderer;
    }

    private SurveyInteractor provideSurveyInteractor() {
        if (surveyInteractor == null) {
            ReportManager reportManager = new ReportManager.DummyReportManager();
            surveyInteractor = new SurveyInteractor(survey, null, reportManager, new Language("en"), Executors.newSingleThreadExecutor(), new AndroidMainThreadExecutor());
        }
        return surveyInteractor;
    }

    private SurveyPresenter provideSurveyPresenter() {
        if (surveyPresenter == null) {
            surveyPresenter = new SurveyPresenter(provideSurveyInteractor(), survey, theme);
        }
        return surveyPresenter;
    }

    private static final class AndroidMainThreadExecutor implements Executor {

        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    }
}
