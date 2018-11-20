/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.model;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Answer implements Serializable {
    private final int id;
    private final String title;
    private final Node nextMap;
    private final String explainType;

    @VisibleForTesting Answer(int id, String title, @Nullable Node nextMap, @Nullable String explainType) {
        this.id = id;
        this.title = title;
        this.nextMap = nextMap;
        this.explainType = explainType;
    }

    @SuppressWarnings("unused") private Answer() {
        this.id = 0;
        this.title = null;
        this.nextMap = null;
        this.explainType = null;
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    @Nullable public Node nextMap() {
        return nextMap;
    }

    @Nullable public String explainType() {
        return explainType;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer = (Answer) o;

        return id == answer.id;
    }

    @Override public int hashCode() {
        return id;
    }
}
