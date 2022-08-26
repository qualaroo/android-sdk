package com.qualaroo.ui;

import android.animation.LayoutTransition;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.core.widget.ImageViewCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.render.Renderer;
import com.qualaroo.ui.render.RestorableView;
import com.qualaroo.ui.render.ViewState;
import com.qualaroo.util.ColorStateListUtils;
import com.qualaroo.util.ContentUtils;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.KeyboardUtil;

import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SurveyFragment extends Fragment implements SurveyView {

    private static final String KEY_PRESENTER_STATE = "pstate";
    private static final String RESTORABLE_VIEW_STATE = "qviewstate";

    private static final String DESCRIPTION_PLACEMENT_BEFORE = "before";
    private static final String DESCRIPTION_PLACEMENT_AFTER = "after";

//    private static final String TYPEFACE_BOLD = "bold";
    private static final String TYPEFACE_ITALIC = "italic";
    private static final String TYPEFACE_OBLIQUE = "oblique";

    SurveyPresenter surveyPresenter;
    Renderer renderer;
    ImageProvider imageProvider;

    private ViewGroup backgroundView;
    private LinearLayout surveyContainer;
    private TextView questionsTitleTop;
    private TextView questionsTitleBottom;
    private FrameLayout questionsContent;
    private ImageView closeButton;
    private ImageView surveyLogo;
    private ProgressBarView progressBar;

    private boolean isFullScreen;
    private RestorableView restorableView;
    private ViewState viewState;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qualaroo__fragment_survey, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backgroundView = view.findViewById(R.id.qualaroo__fragment_survey_container);
        questionsTitleTop = view.findViewById(R.id.qualaroo__question_title_top);
        questionsTitleBottom = view.findViewById(R.id.qualaroo__question_title_bottom);
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
        questionsTitleTop.setTextColor(viewModel.textColor());
        questionsTitleBottom.setTextColor(viewModel.textColor());
        surveyContainer.setBackgroundColor(viewModel.backgroundColor());
        ImageViewCompat.setImageTintList(closeButton, ColorStateListUtils.enabledButton(viewModel.uiNormal(), viewModel.uiSelected()));
        closeButton.setVisibility(viewModel.cannotBeClosed() ? View.INVISIBLE : View.VISIBLE);
        backgroundView.setAlpha(0.0f);
        int dimColor = calculateDimColor(viewModel.dimColor(), viewModel.dimOpacity());
        backgroundView.setBackgroundColor(dimColor);
        isFullScreen = viewModel.isFullscreen();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            surveyContainer.getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGING);
        }
        if (isFullScreen) {
            ViewGroup.LayoutParams surveyLayoutParams = surveyContainer.getLayoutParams();
            surveyLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surveyContainer.setLayoutParams(surveyLayoutParams);
            surveyContainer.setGravity(Gravity.CENTER);
        }
        ViewCompat.setBackgroundTintList(surveyLogo, ColorStateList.valueOf(viewModel.backgroundColor()));
        imageProvider.getImage(viewModel.logoUrl(), new ImageProvider.OnBitmapLoadedListener() {
            @Override public void onBitmapReady(Bitmap bitmap) {
                surveyLogo.setImageBitmap(bitmap);
            }
        });
        progressBar = new ProgressBarView(getContext(), null);
        setupProgressBar(progressBar, viewModel);
    }

    public void setupProgressBar(ProgressBarView progressBar, SurveyViewModel viewModel) {
        if (viewModel.progressBarPosition() == ProgressBarPosition.NONE) return;
        progressBar.setColors(viewModel.uiSelected(), viewModel.uiNormal());
        int height = getResources().getDimensionPixelSize(R.dimen.qualaroo__progress_bar_height);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        if (viewModel.isFullscreen()) {
            int gravity = viewModel.progressBarPosition() == ProgressBarPosition.TOP ? Gravity.TOP : Gravity.BOTTOM;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(progressBar.getLayoutParams());
            params.gravity = gravity;
            progressBar.setLayoutParams(params);
            backgroundView.addView(progressBar);
        } else {
            boolean addAsLast = viewModel.progressBarPosition() == ProgressBarPosition.BOTTOM;
            surveyContainer.addView(progressBar, addAsLast ? surveyContainer.getChildCount() : 0);
        }
    }

    private int calculateDimColor(@ColorInt int dimColor, float dimOpacity) {
        int alpha = Color.alpha(dimColor);
        int newAlpha = (int) (alpha * dimOpacity);
        return Color.argb(newAlpha, Color.red(dimColor), Color.green(dimColor), Color.blue(dimColor));
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
        String title = ContentUtils.sanitazeText(question.title());
        String description = ContentUtils.sanitazeText(question.description());
        questionsTitleTop.setTextSize(22);
        if (description != null && description.length() > 0) {
            if (DESCRIPTION_PLACEMENT_BEFORE.equals(question.descriptionPlacement())) {
                questionsTitleBottom.setVisibility(View.VISIBLE);
                questionsTitleBottom.setText(title);
                setFontType(question.getFont_style_question(),questionsTitleBottom);
                questionsTitleTop.setText(description);
                setFontType(question.getFont_style_description(),questionsTitleTop);
            } else if (DESCRIPTION_PLACEMENT_AFTER.equals(question.descriptionPlacement())) {
                questionsTitleBottom.setVisibility(View.VISIBLE);
                questionsTitleBottom.setText(description);
                setFontType(question.getFont_style_description(),questionsTitleBottom);
                questionsTitleTop.setText(title);
                setFontType(question.getFont_style_question(),questionsTitleTop);
            }
        } else {
            questionsTitleBottom.setVisibility(View.GONE);
            questionsTitleTop.setText(title);
            setFontType(question.getFont_style_question(),questionsTitleTop);
        }
        restorableView = renderer.renderQuestion(getContext(), question, new OnAnsweredListener() {
            @Override public void onResponse(UserResponse userResponse) {
                surveyPresenter.onResponse(userResponse);
            }
        });
        questionsContent.addView(restorableView.view());
        if (viewState != null) {
            restorableView.restoreState(viewState);
        }
    }

    void setFontType(String fontType, TextView textView){
        switch (fontType){
//            case TYPEFACE_BOLD:
//                textView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
//                break;
            case TYPEFACE_ITALIC:
                textView.setTypeface(Typeface.DEFAULT,Typeface.ITALIC);
                break;
            case TYPEFACE_OBLIQUE:
                textView.setTypeface(Typeface.DEFAULT,Typeface.BOLD_ITALIC);
                break;
            default:
                textView.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
        }
    }

    @Override public void showMessage(Message message, boolean withAnimation) {
        restorableView = null;
        transformToMessageStyle(withAnimation);
        questionsContent.removeAllViews();
        questionsContent.addView(renderer.renderMessage(getContext(), message, new OnMessageConfirmedListener() {
            @Override public void onMessageConfirmed(Message message) {
                surveyPresenter.onMessageConfirmed(message);
            }
        }));
    }

    @Override public void showLeadGen(QScreen qscreen, List<Question> questions) {
        questionsTitleBottom.setVisibility(View.GONE);
        transformToQuestionStyle();
        questionsContent.removeAllViews();
        questionsTitleTop.setText(ContentUtils.sanitazeText(qscreen.description()));
        setFontType(qscreen.getFont_style_description(),questionsTitleTop);
        restorableView = renderer.renderLeadGen(getContext(), qscreen, questions, new OnLeadGenAnswerListener() {
            @Override public void onResponse(List<UserResponse> userResponses) {
                surveyPresenter.onLeadGenResponse(userResponses);
            }
        });
        questionsContent.addView(restorableView.view());
        if (viewState != null) {
            restorableView.restoreState(viewState);
        }
    }

    @Override public void setProgress(float progress) {
        progressBar.setProgress(progress);
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
        questionsTitleTop.setText(null);
        questionsTitleBottom.setVisibility(View.GONE);
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
                    questionsTitleTop.animate().alpha(alpha).start();
                    surveyLogo.animate().scaleX(1.5f);
                    surveyLogo.animate().scaleY(1.5f);
                } else {
                    surveyLogo.setTranslationX(translationX);
                    surveyLogo.setTranslationY(translationY);
                    surveyLogo.setScaleX(1.5f);
                    surveyLogo.setScaleY(1.5f);
                    questionsTitleTop.setAlpha(alpha);
                }
            }
        });
    }

    private void transformToQuestionStyle() {
        surveyLogo.animate().translationY(0).translationX(0).start();
        questionsTitleTop.animate().alpha(1.0f).start();
        surveyLogo.animate().scaleX(1.0f);
        surveyLogo.animate().scaleY(1.0f);
    }

    @Override public void closeSurvey() {
        runCloseAnimation();
        surveyContainer.postDelayed(new Runnable() {
            @Override public void run() {
                if (getActivity() != null) {
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        }, 600);
    }

    public void onBackPressed() {
        surveyPresenter.onCloseClicked();
    }

}
