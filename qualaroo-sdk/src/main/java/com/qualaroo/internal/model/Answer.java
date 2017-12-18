package com.qualaroo.internal.model;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Answer implements Serializable {
    private int id;
    private String title;
    private Node nextMap;
    private String explainType;

    @VisibleForTesting Answer(int id, String title, Node nextMap, String explainType) {
        this.id = id;
        this.title = title;
        this.nextMap = nextMap;
        this.explainType = explainType;
    }

    @SuppressWarnings("unused") Answer() {
        //deserializing with gson requires a default constructor
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
