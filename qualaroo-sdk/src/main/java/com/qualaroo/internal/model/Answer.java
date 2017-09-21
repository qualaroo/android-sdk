package com.qualaroo.internal.model;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

public final class Answer implements Serializable {
    private int id;
    private String title;
    private Node nextMap;

    @VisibleForTesting Answer(int id, String title, Node nextMap) {
        this.id = id;
        this.title = title;
        this.nextMap = nextMap;
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
