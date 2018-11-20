/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui.render.widget;

import android.widget.Checkable;
import android.widget.CompoundButton;

public interface ListeningCheckable extends Checkable {
    void setOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener listener);
}
