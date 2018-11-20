/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.executor;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.util.concurrent.Executor;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class UiThreadExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void execute(@NonNull Runnable command) {
        handler.post(command);
    }
}
