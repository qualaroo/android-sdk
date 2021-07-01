package com.qualaroo.util;

import androidx.annotation.RestrictTo;

import java.util.Collections;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Shuffler {
    public void shuffle(List<?> list) {
        Collections.shuffle(list);
    }
}
