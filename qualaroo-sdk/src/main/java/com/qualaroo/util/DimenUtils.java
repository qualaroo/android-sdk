package com.qualaroo.util;

import android.content.Context;
import android.support.annotation.DimenRes;

public class DimenUtils {

    public static int px(Context c, @DimenRes int dimenRes) {
        return c.getResources().getDimensionPixelSize(dimenRes);
    }

}
