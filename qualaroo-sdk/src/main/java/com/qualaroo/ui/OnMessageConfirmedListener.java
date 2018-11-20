/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Message;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnMessageConfirmedListener {
    void onMessageConfirmed(Message message);
}
