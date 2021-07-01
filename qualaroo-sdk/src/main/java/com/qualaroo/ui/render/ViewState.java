package com.qualaroo.ui.render;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class ViewState implements Parcelable {

    private final long viewUniqueId;
    private final Bundle bundle;

    ViewState(long viewUniqueId, Bundle bundle) {
        this.viewUniqueId = viewUniqueId;
        this.bundle = bundle;
    }

    long viewUniqueId() {
        return viewUniqueId;
    }

    Bundle bundle() {
        return bundle;
    }

    protected ViewState(Parcel in) {
        viewUniqueId = in.readInt();
        bundle = in.readBundle(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(viewUniqueId);
        dest.writeBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ViewState> CREATOR = new Creator<ViewState>() {
        @Override
        public ViewState createFromParcel(Parcel in) {
            return new ViewState(in);
        }

        @Override
        public ViewState[] newArray(int size) {
            return new ViewState[size];
        }
    };

}
