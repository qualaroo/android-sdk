package com.qualaroo.internal.event;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.qualaroo.QualarooSurveyEventReceiver;

public class SurveyEvent implements Parcelable {

    public static SurveyEvent dismissed(String alias) {
        return new SurveyEvent(alias, QualarooSurveyEventReceiver.EVENT_TYPE_DISMISSED);
    }

    public static SurveyEvent shown(String alias) {
        return new SurveyEvent(alias, QualarooSurveyEventReceiver.EVENT_TYPE_SHOWN);
    }

    public static SurveyEvent finished(String alias) {
        return new SurveyEvent(alias, QualarooSurveyEventReceiver.EVENT_TYPE_FINISHED);
    }

    private final String alias;
    private final int type;

    private SurveyEvent(@NonNull String surveyAlias, @QualarooSurveyEventReceiver.Type int type) {
        this.alias = surveyAlias;
        this.type = type;
    }

    public String alias() {
        return alias;
    }

    @QualarooSurveyEventReceiver.Type public int type() {
        return type;
    }

    protected SurveyEvent(Parcel in) {
        alias = in.readString();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alias);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SurveyEvent> CREATOR = new Creator<SurveyEvent>() {
        @Override
        public SurveyEvent createFromParcel(Parcel in) {
            return new SurveyEvent(in);
        }

        @Override
        public SurveyEvent[] newArray(int size) {
            return new SurveyEvent[size];
        }
    };

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyEvent that = (SurveyEvent) o;

        return type == that.type && alias.equals(that.alias);
    }

    @Override public int hashCode() {
        int result = alias.hashCode();
        result = 31 * result + type;
        return result;
    }

    @Override public String toString() {
        return "SurveyEvent{" +
                "alias='" + alias + '\'' +
                ", type=" + type +
                '}';
    }
}
