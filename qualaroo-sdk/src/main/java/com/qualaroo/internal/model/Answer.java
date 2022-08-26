package com.qualaroo.internal.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Answer implements Serializable {
    private final int id;
    private final String title;
    private final Node nextMap;
    private final String emoji_url;
    private final String explainType;

    @VisibleForTesting Answer(int id, String title, @Nullable Node nextMap, @Nullable String explainType, @Nullable String emoji_url) {
        this.id = id;
        this.title = title;
        this.nextMap = nextMap;
        this.explainType = explainType;
        this.emoji_url = emoji_url;
    }

    @SuppressWarnings("unused") private Answer() {
        this.id = 0;
        this.title = null;
        this.nextMap = null;
        this.explainType = null;
        this.emoji_url =null;
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

    @Nullable public  String emojiUrl() { return emoji_url;}

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
