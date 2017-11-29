package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.ui.render.Renderer;
import com.qualaroo.ui.render.Theme;
import com.qualaroo.util.UriOpener;

import java.util.concurrent.Executor;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SurveyComponent {

    private final SurveyInteractor surveyInteractor;
    private final SurveyPresenter surveyPresenter;
    private final Renderer renderer;
    private final ImageProvider imageProvider;

    public static SurveyComponent from(Survey survey, LocalStorage localStorage, ReportManager reportManager, Language preferredLanguage, Executor backgroundExecutor, Executor uiExecutor, UriOpener uriOpener, ImageProvider imageProvider) {
        return new SurveyComponent(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor, uriOpener, imageProvider);
    }

    private SurveyComponent(Survey survey, LocalStorage localStorage, ReportManager reportManager, Language preferredLanguage, Executor backgroundExecutor, Executor uiExecutor, UriOpener uriOpener, ImageProvider imageProvider) {
        Theme theme = Theme.from(survey.spec().optionMap().colorThemeMap());
        this.surveyInteractor = new SurveyInteractor(survey, localStorage,  reportManager, preferredLanguage, backgroundExecutor, uiExecutor);
        this.surveyPresenter = new SurveyPresenter(surveyInteractor, survey, theme, uriOpener);
        this.renderer = new Renderer(theme);
        this.imageProvider = imageProvider;
    }

    void inject(SurveyFragment surveyFragment) {
        surveyFragment.surveyPresenter = this.surveyPresenter;
        surveyFragment.renderer = this.renderer;
        surveyFragment.imageProvider = this.imageProvider;
    }

}
