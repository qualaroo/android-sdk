package com.qualaroo.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.render.Renderer;
import com.qualaroo.ui.render.Theme;
import com.qualaroo.util.DebouncingOnClickListener;

import java.util.List;

public class SurveyFragment extends Fragment implements SurveyView {

    SurveyPresenter surveyPresenter;
    Renderer renderer;
    Theme theme;

    private View surveyContainer;
    private TextView questionsTitle;
    private FrameLayout questionsContent;
    private ImageView closeButton;
    private ImageView surveyLogo;
    private View emptySpace;

    private boolean mandatory;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qualaroo__fragment_survey, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        questionsTitle = view.findViewById(R.id.qualaroo__question_title);
        questionsContent = view.findViewById(R.id.qualaroo__question_content);
        surveyContainer = view.findViewById(R.id.qualaroo__survey_container);
        surveyLogo = view.findViewById(R.id.qualaroo__survey_logo);
        emptySpace = view.findViewById(R.id.qualaroo__survey_empty_space);
        try {
            Drawable applicationIcon = getContext().getPackageManager().getApplicationIcon(getContext().getPackageName());
            surveyLogo.setImageDrawable(applicationIcon);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
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
        runOpenAnimation();
    }

    private void runOpenAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(250);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                getView().setAlpha(value);
            }
        });
        animator.start();
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

    private void runCloseAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(300);
        animator.setStartDelay(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                getView().setAlpha(value);
            }
        });
        animator.start();
        surveyContainer.animate()
                .setDuration(300)
                .translationY(surveyContainer.getHeight())
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    @Override public void onDestroyView() {
        surveyPresenter.dropView();
        super.onDestroyView();
    }

    @Override public void setup(SurveyViewModel viewModel) {
        questionsTitle.setTextColor(viewModel.textColor());
        ((View) questionsContent.getParent()).setBackgroundColor(viewModel.backgroundColor());
        closeButton.setColorFilter(viewModel.buttonDisabledColor());
        closeButton.setVisibility(viewModel.cannotBeClosed() ? View.GONE : View.VISIBLE);
        emptySpace.setVisibility(viewModel.isFullscreen() ? View.GONE : View.VISIBLE);
        mandatory = viewModel.cannotBeClosed();
    }

    @Override public void showQuestion(Question question) {
        transformToQuestionStyle();
        questionsContent.removeAllViews();
        questionsTitle.setText(question.title());
        Context context = getContext();
        View view = renderer.renderQuestion(context, question, new OnAnsweredListener() {
            @Override public void onAnswered(Question question, Answer answer) {
                surveyPresenter.onAnswered(answer);
            }

            @Override public void onAnswered(Question question, List<Answer> answers) {
                surveyPresenter.onAnswered(answers);
            }

            @Override public void onAnsweredWithText(Question question, String answer) {
                surveyPresenter.onAnsweredWithText(answer);
            }
        });
        questionsContent.addView(view);
    }

    @Override public void showMessage(Message message) {
        transformToMessageStyle();
        questionsContent.removeAllViews();
        Context context = getContext();
        questionsContent.addView(renderer.renderMessage(context, message, new OnMessageConfirmedListener() {
            @Override public void onMessageConfirmed(Message message) {
                surveyPresenter.onCloseClicked();
            }
        }));
    }

    private void transformToMessageStyle() {
        surveyLogo.animate()
                .translationX(surveyContainer.getWidth()/2 - surveyLogo.getX() - surveyLogo.getWidth()/2)
                .translationY(-surveyLogo.getY() - surveyLogo.getHeight()/2)
                .start();
        questionsTitle.animate().alpha(0.0f).start();
    }

    private void transformToQuestionStyle() {
        surveyLogo.animate().translationY(0).translationX(0).start();
        questionsTitle.animate().alpha(1.0f).start();
    }

    @Override public void closeSurvey() {
        runCloseAnimation();
        getView().postDelayed(new Runnable() {
            @Override public void run() {
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
            }
        }, 600);
    }

    /**
     * Callback for onBackPressed event happening in containing Activity.
     * @return true if SurveyFragment consumed the event, false if Activity should handle the event by itself
     */
    public boolean onBackPressed() {
        boolean shouldBlockClosing = mandatory;
        return shouldBlockClosing;
    }
}
