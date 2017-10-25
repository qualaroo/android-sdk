package com.qualaroo.ui.render;

import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.view.View;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class QuestionView {

    static Builder forQuestionId(long questionId) {
        return new Builder()
                .setQuestionId(questionId);
    }

    interface OnRestoreState {
        void onRestoreState(Bundle from);
    }
    interface OnSaveState {
        void onSaveState(Bundle into);
    }

    private final long questionId;
    private final View view;

    private final OnRestoreState onRestoreState;
    private final OnSaveState onSaveState;

    QuestionView(long questionId, View view, OnSaveState onSaveState, OnRestoreState onRestoreState) {
        this.questionId = questionId;
        this.view = view;
        this.onRestoreState = onRestoreState;
        this.onSaveState = onSaveState;
    }

    public View view() {
        return view;
    }

    public void restoreState(QuestionViewState questionViewState) {
        if (questionId == questionViewState.questionId() && onRestoreState != null) {
            onRestoreState.onRestoreState(questionViewState.bundle());
        }
    }

    public QuestionViewState getCurrentState() {
        Bundle bundle = new Bundle();
        if (onSaveState != null) {
            onSaveState.onSaveState(bundle);
        }
        return new QuestionViewState(questionId, bundle);
    }

    static class Builder {

        private long questionId;
        private View view;
        private OnSaveState onSaveState;
        private OnRestoreState onRestoreState;

        Builder setQuestionId(long questionId) {
            this.questionId = questionId;
            return this;
        }

        Builder setView(View view) {
            this.view = view;
            return this;
        }

        Builder onSaveState(OnSaveState onSaveState) {
            this.onSaveState = onSaveState;
            return this;
        }

        Builder onRestoreState(OnRestoreState onRestoreState) {
            this.onRestoreState = onRestoreState;
            return this;
        }

        QuestionView build() {
            return new QuestionView(questionId, view, onSaveState, onRestoreState);
        }
    }


}
