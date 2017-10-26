package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QScreen implements Serializable{
    private long id;
    private List<Long> questionList;
    private String description;
    private String sendText;
    private Node nextMap;

    @VisibleForTesting QScreen(long id, List<Long> questionList, String description, String sendText, Node nextMap) {
        this.id = id;
        this.questionList = questionList;
        this.description = description;
        this.sendText = sendText;
        this.nextMap = nextMap;
    }

    @SuppressWarnings("unused") QScreen() {

    }

    public long id() {
        return id;
    }

    public List<Long> questionList() {
        return questionList;
    }

    public String description() {
        return description;
    }

    public String sendText() {
        return sendText;
    }

    public Node nextMap() {
        return nextMap;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QScreen qScreen = (QScreen) o;

        return id == qScreen.id;
    }

    @Override public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
