/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

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
