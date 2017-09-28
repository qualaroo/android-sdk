package com.qualaroo.internal.executor;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class UiThreadExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void execute(@NonNull Runnable command) {
        handler.post(command);
    }
}
