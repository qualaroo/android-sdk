package com.qualaroo.util;

import android.support.annotation.RestrictTo;

import java.util.Collections;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Shuffler {
    public void shuffle(List<?> list) {
        Collections.shuffle(list);
    }
}
