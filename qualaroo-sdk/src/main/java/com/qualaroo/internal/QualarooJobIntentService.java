package com.qualaroo.internal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

public class QualarooJobIntentService extends JobIntentService {
    @Override protected void onHandleWork(@NonNull Intent intent) {

    }

    @Override public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }
}
