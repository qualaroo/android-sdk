package com.qualaroo.ui.render.widget;

import android.widget.Checkable;
import android.widget.CompoundButton;

public interface ListeningCheckable extends Checkable {
    void setOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener listener);
}
