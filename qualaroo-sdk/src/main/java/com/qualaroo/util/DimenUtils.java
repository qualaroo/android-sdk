package com.qualaroo.util;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class DimenUtils {

    public static int px(Context c, @DimenRes int dimenRes) {
        return c.getResources().getDimensionPixelSize(dimenRes);
    }

}
