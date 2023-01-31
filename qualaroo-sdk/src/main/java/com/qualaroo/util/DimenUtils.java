package com.qualaroo.util;

import android.content.Context;
import androidx.annotation.DimenRes;
import androidx.annotation.RestrictTo;
import android.util.TypedValue;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class DimenUtils {

    public static int px(Context c, int dimenRes) {
        return c.getResources().getDimensionPixelSize(dimenRes);
    }

    public static int pxToDp(Context c, int px) {
        return (int) (px / c.getResources().getDisplayMetrics().density);
    }

    public static float toPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
