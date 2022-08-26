package com.qualaroo.internal.model;

import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QScreen implements Serializable{
    private final long id;
    private final List<Long> questionList;
    private final String description;
    private final String font_style_description;
    private final String sendText;
    private final Node nextMap;

    @VisibleForTesting QScreen(long id, List<Long> questionList, String description,String font_style_description,String sendText, Node nextMap) {
        this.id = id;
        this.questionList = questionList;
        this.description = description;
        this.font_style_description = font_style_description;
        this.sendText = sendText;
        this.nextMap = nextMap;
    }

    @SuppressWarnings("unused") private QScreen() {

        this.id = 0;
        this.questionList = null;
        this.description = null;
        this.font_style_description = null;
        this.sendText = null;
        this.nextMap = null;
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

    public String getFont_style_description() {
        return font_style_description;
    }

    public String sendText() {
        return sendText;
    }

    public Node nextMap() {
        return nextMap;
    }

    public QScreen copy(String description) {
        return new QScreen(id, questionList, description, font_style_description, sendText, nextMap);
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
