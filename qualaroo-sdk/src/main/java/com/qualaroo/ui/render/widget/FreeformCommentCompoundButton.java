package com.qualaroo.ui.render.widget;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.qualaroo.ui.render.Theme;
import com.qualaroo.ui.render.ThemeUtils;

public class FreeformCommentCompoundButton extends LinearLayout implements ListeningCheckable {

    private final EditText freeformComment;
    private final CompoundButton compoundButton;

    public FreeformCommentCompoundButton(Context context, CompoundButton compoundButton) {
        super(context);
        setOrientation(VERTICAL);
        this.freeformComment = new AppCompatEditText(context);
        this.freeformComment.setVisibility(View.GONE);
        this.compoundButton = compoundButton;
        setLayoutTransition(new LayoutTransition());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }
        addView(compoundButton);
        addView(freeformComment);
    }

    @Override
    public void setOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener listener) {
        compoundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onCheckedChanged(buttonView, isChecked);
                if (isChecked) {
                    freeformComment.setVisibility(VISIBLE);
                    freeformComment.requestFocus();
                } else {
                    freeformComment.setVisibility(GONE);
                }
            }
        });
    }

    @Override public void setChecked(boolean checked) {
        compoundButton.setChecked(checked);
    }

    @Override public boolean isChecked() {
        return compoundButton.isChecked();
    }

    @Override public void toggle() {
        compoundButton.toggle();
    }

    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        compoundButton.setEnabled(enabled);
    }

    public void acceptTheme(Theme theme) {
        ThemeUtils.applyTheme(compoundButton, theme);
        ThemeUtils.applyTheme(freeformComment, theme);
    }

    public String getText() {
        return freeformComment.getText().toString();
    }

    public State getState() {
        return new State(getId(), freeformComment.getText().toString());
    }

    public void restoreState(State state) {
        freeformComment.setText(state.text);
    }

    public static class State implements Parcelable {
        public final int id;
        public final String text;

        State(int id, String text) {
            this.id = id;
            this.text = text;
        }

        protected State(Parcel in) {
            id = in.readInt();
            text = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(text);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }
}
