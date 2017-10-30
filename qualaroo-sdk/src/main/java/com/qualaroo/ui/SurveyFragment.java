package com.qualaroo.ui;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.render.RestorableView;
import com.qualaroo.ui.render.ViewState;
import com.qualaroo.ui.render.Renderer;
import com.qualaroo.util.ContentUtils;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.KeyboardUtil;

import java.util.List;
import java.util.Map;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SurveyFragment extends Fragment implements SurveyView {

    private static final String KEY_PRESENTER_STATE = "pstate";
    private static final String RESTORABLE_VIEW_STATE = "qviewstate";

    SurveyPresenter surveyPresenter;
    Renderer renderer;

    private View backgroundView;
    private View surveyContainer;
    private TextView questionsTitle;
    private FrameLayout questionsContent;
    private ImageView closeButton;
    private ImageView surveyLogo;

    private boolean isFullScreen;
    private RestorableView restorableView;
    private ViewState viewState;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qualaroo__fragment_survey, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backgroundView = view.findViewById(R.id.qualaroo__fragment_survey_container);
        questionsTitle = view.findViewById(R.id.qualaroo__question_title);
        questionsContent = view.findViewById(R.id.qualaroo__question_content);
        surveyContainer = view.findViewById(R.id.qualaroo__survey_container);
        surveyLogo = view.findViewById(R.id.qualaroo__survey_logo);
        try {
            Drawable applicationIcon = getContext().getPackageManager().getApplicationIcon(getContext().getPackageName());
            surveyLogo.setImageDrawable(applicationIcon);
        } catch (PackageManager.NameNotFoundException e) {
            //ignore exception
        }
        closeButton = view.findViewById(R.id.qualaroo__survey_close);
        closeButton.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                surveyPresenter.onCloseClicked();
            }
        });
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SurveyComponentHelper.get(getContext()).inject(this);
        surveyPresenter.setView(this);
        SurveyPresenter.State presentersState = null;
        if (savedInstanceState != null) {
            presentersState = (SurveyPresenter.State) savedInstanceState.getSerializable(KEY_PRESENTER_STATE);
            viewState = savedInstanceState.getParcelable(RESTORABLE_VIEW_STATE);
        }
        surveyPresenter.init(presentersState);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        if (restorableView != null) {
            outState.putParcelable(RESTORABLE_VIEW_STATE, restorableView.getCurrentState());
        }
        outState.putSerializable(KEY_PRESENTER_STATE, surveyPresenter.getSavedState());
        super.onSaveInstanceState(outState);
    }

    private void runCloseAnimation() {
        backgroundView.animate()
                .alpha(0.0f)
                .setStartDelay(300)
                .setDuration(300)
                .start();
        surveyContainer.animate()
                .setDuration(300)
                .translationY(surveyContainer.getHeight())
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    @Override public void onDestroyView() {
        restorableView = null;
        KeyboardUtil.hideKeyboard(surveyContainer);
        surveyPresenter.dropView();
        super.onDestroyView();
    }

    @Override public void setup(SurveyViewModel viewModel) {
        questionsTitle.setTextColor(viewModel.textColor());
        ((View) questionsContent.getParent()).setBackgroundColor(viewModel.backgroundColor());
        closeButton.setColorFilter(viewModel.buttonDisabledColor());
        closeButton.setVisibility(viewModel.cannotBeClosed() ? View.GONE : View.VISIBLE);
        backgroundView.setAlpha(0.0f);
        backgroundView.setBackgroundColor(viewModel.dimColor());
        isFullScreen = viewModel.isFullscreen();
        if (isFullScreen) {
            ViewGroup.LayoutParams surveyLayoutParams = surveyContainer.getLayoutParams();
            surveyLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surveyContainer.setLayoutParams(surveyLayoutParams);

            LinearLayout.LayoutParams questionLayoutParams = (LinearLayout.LayoutParams) questionsContent.getLayoutParams();
            questionLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            questionsContent.setLayoutParams(questionLayoutParams);
        }
    }

    @Override public void showWithAnimation() {
        backgroundView.setAlpha(0.0f);
        backgroundView.animate()
                .alpha(1.0f)
                .setDuration(300)
                .start();
        surveyContainer.post(new Runnable() {
            @Override public void run() {
                surveyContainer.setTranslationY(surveyContainer.getHeight());
                surveyContainer.setVisibility(View.VISIBLE);
                surveyContainer.animate()
                        .setStartDelay(250)
                        .setDuration(300)
                        .translationY(0)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .start();
            }
        });
    }

    @Override public void showImmediately() {
        backgroundView.setAlpha(1.0f);
        surveyContainer.setVisibility(View.VISIBLE);
        surveyContainer.setTranslationY(0);
    }

    @Override public void showQuestion(Question question) {
        transformToQuestionStyle();
        questionsContent.removeAllViews();
        questionsTitle.setText(question.title());
        restorableView = renderer.renderQuestion(getContext(), question, new OnAnsweredListener() {
            @Override public void onAnswered(Answer answer) {
                surveyPresenter.onAnswered(answer);
            }

            @Override public void onAnswered(List<Answer> answers) {
                surveyPresenter.onAnswered(answers);
            }

            @Override public void onAnsweredWithText(String answer) {
                surveyPresenter.onAnsweredWithText(answer);
            }
        });
        questionsContent.addView(restorableView.view());
        if (viewState != null) {
            restorableView.restoreState(viewState);
        }
    }

    @Override public void showMessage(Message message, boolean withAnimation) {
        restorableView = null;
        transformToMessageStyle(withAnimation);
        questionsContent.removeAllViews();
        questionsContent.addView(renderer.renderMessage(getContext(), message, new OnMessageConfirmedListener() {
            @Override public void onMessageConfirmed(Message message) {
                surveyPresenter.onCloseClicked();
            }
        }));
    }

    @Override public void showLeadGen(QScreen qscreen, List<Question> questions) {
        transformToQuestionStyle();
        questionsContent.removeAllViews();
        questionsTitle.setText(ContentUtils.sanitazeText(qscreen.description()));
        questionsContent.addView(renderer.renderLeadGen(getContext(), qscreen, questions, new OnLeadGenAnswerListener() {
            @Override public void onLeadGenAnswered(Map<Long, String> questionIdsWithAnswers) {
                surveyPresenter.onLeadGenAnswered(questionIdsWithAnswers);
            }
        }));
    }

    @Override public void forceShowKeyboardWithDelay(long delayInMillis) {
        final EditText editText = findEditText(questionsContent);
        if (editText != null) {
            editText.postDelayed(new Runnable() {
                @Override public void run() {
                    KeyboardUtil.showKeyboard(editText);
                }
            }, delayInMillis);
        }
    }

    @Nullable private EditText findEditText(View view) {
        if (view instanceof EditText) {
            return (EditText) view;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                EditText result = findEditText(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void transformToMessageStyle(final boolean withAnimation) {
        questionsTitle.setText(null);
        surveyContainer.post(new Runnable() {
            @Override public void run() {
                float translationX = surveyContainer.getWidth() / 2 - surveyLogo.getX() - surveyLogo.getWidth() / 2;
                float translationY = isFullScreen ? 0 : -surveyLogo.getY() - surveyLogo.getHeight() / 2;
                float alpha = 0.0f;
                if (withAnimation) {
                    surveyLogo.animate()
                            .translationX(translationX)
                            .translationY(translationY)
                            .start();
                    questionsTitle.animate().alpha(alpha).start();
                    surveyLogo.animate().scaleX(1.5f);
                    surveyLogo.animate().scaleY(1.5f);
                } else {
                    surveyLogo.setTranslationX(translationX);
                    surveyLogo.setTranslationY(translationY);
                    surveyLogo.setScaleX(1.5f);
                    surveyLogo.setScaleY(1.5f);
                    questionsTitle.setAlpha(alpha);
                }
            }
        });
    }

    private void transformToQuestionStyle() {
        surveyLogo.animate().translationY(0).translationX(0).start();
        questionsTitle.animate().alpha(1.0f).start();
        surveyLogo.animate().scaleX(1.0f);
        surveyLogo.animate().scaleY(1.0f);
    }

    @Override public void closeSurvey() {
        runCloseAnimation();
        surveyContainer.postDelayed(new Runnable() {
            @Override public void run() {
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
            }
        }, 600);
    }

    public void onBackPressed() {
        surveyPresenter.onCloseClicked();
    }
}
