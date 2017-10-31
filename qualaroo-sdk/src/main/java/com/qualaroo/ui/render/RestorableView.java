package com.qualaroo.ui.render;

import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.view.View;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class RestorableView {

    static Builder withId(long viewUniqueId) {
        return new Builder()
                .viewId(viewUniqueId);
    }

    interface OnRestoreState {
        void onRestoreState(Bundle from);
    }
    interface OnSaveState {
        void onSaveState(Bundle into);
    }

    private final long viewUniqueId;
    private final View view;

    private final OnRestoreState onRestoreState;
    private final OnSaveState onSaveState;

    RestorableView(long viewUniqueId, View view, OnSaveState onSaveState, OnRestoreState onRestoreState) {
        this.viewUniqueId = viewUniqueId;
        this.view = view;
        this.onRestoreState = onRestoreState;
        this.onSaveState = onSaveState;
    }

    public View view() {
        return view;
    }

    public void restoreState(ViewState viewState) {
        if (viewUniqueId == viewState.viewUniqueId() && onRestoreState != null) {
            onRestoreState.onRestoreState(viewState.bundle());
        }
    }

    public ViewState getCurrentState() {
        Bundle bundle = new Bundle();
        if (onSaveState != null) {
            onSaveState.onSaveState(bundle);
        }
        return new ViewState(viewUniqueId, bundle);
    }

    static class Builder {

        private long viewId;
        private View view;
        private OnSaveState onSaveState;
        private OnRestoreState onRestoreState;

        Builder viewId(long viewId) {
            this.viewId = viewId;
            return this;
        }

        Builder view(View view) {
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

        RestorableView build() {
            return new RestorableView(viewId, view, onSaveState, onRestoreState);
        }
    }


}
