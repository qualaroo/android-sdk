package com.qualaroo.ui.render;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class QuestionViewState implements Parcelable {

    private final long questionId;
    private final Bundle bundle;

    QuestionViewState(long questionId, Bundle bundle) {
        this.questionId = questionId;
        this.bundle = bundle;
    }

    long questionId() {
        return questionId;
    }

    Bundle bundle() {
        return bundle;
    }

    protected QuestionViewState(Parcel in) {
        questionId = in.readInt();
        bundle = in.readBundle(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(questionId);
        dest.writeBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QuestionViewState> CREATOR = new Creator<QuestionViewState>() {
        @Override
        public QuestionViewState createFromParcel(Parcel in) {
            return new QuestionViewState(in);
        }

        @Override
        public QuestionViewState[] newArray(int size) {
            return new QuestionViewState[size];
        }
    };

}
