package com.qualaroo.internal.model;


import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Question implements Serializable {
    private final long id;
    private final QuestionType type;
    private final String title;
    @Nullable private final String description;
    private final String descriptionPlacement;
    private final List<Answer> answerList;
    private final String sendText;
    private final Node nextMap;
    private final String npsMinLabel;
    private final String npsMaxLabel;
    private final String cname;
    private final boolean disableRandom;
    private final boolean anchorLast;
    private final int anchorLastCount;
    private final boolean alwaysShowSend;
    private final boolean isRequired;
    private final int minAnswersCount;
    private final int maxAnswersCount;
    private final String fontStyleQuestion;
    private final String fontStyleDescription;

    @VisibleForTesting Question(long id, QuestionType type, String title, String description,
                                String descriptionPlacement, List<Answer> answerList, String sendText, Node nextMap,
                                String npsMinLabel, String npsMaxLabel, String cname, boolean disableRandom,
                                boolean anchorLast, int anchorLastCount, boolean alwaysShowSend, boolean isRequired,
                                int minAnswersCount, int maxAnswersCount, String fontStyleQuestion, String fontStyleDescription) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.descriptionPlacement = descriptionPlacement;
        this.answerList = answerList;
        this.sendText = sendText;
        this.nextMap = nextMap;
        this.npsMinLabel = npsMinLabel;
        this.npsMaxLabel = npsMaxLabel;
        this.disableRandom = disableRandom;
        this.anchorLast = anchorLast;
        this.anchorLastCount = anchorLastCount;
        this.alwaysShowSend = alwaysShowSend;
        this.isRequired = isRequired;
        this.cname = cname;
        this.minAnswersCount = minAnswersCount;
        this.maxAnswersCount = maxAnswersCount;
        this.fontStyleQuestion = fontStyleQuestion;
        this.fontStyleDescription = fontStyleDescription;
    }

    @SuppressWarnings("unused") private Question() {
        this.id = 0;
        this.type = null;
        this.title = null;
        this.description = null;
        this.descriptionPlacement = null;
        this.answerList = null;
        this.sendText = null;
        this.nextMap = null;
        this.npsMinLabel = null;
        this.npsMaxLabel = null;
        this.disableRandom = false;
        this.anchorLast = false;
        this.anchorLastCount = 0;
        this.alwaysShowSend = false;
        this.isRequired = false;
        this.cname = null;
        this.minAnswersCount = 0;
        this.maxAnswersCount = 0;
        this.fontStyleQuestion = "normal";
        this.fontStyleDescription ="normal";
    }

    public long id() {
        return id;
    }

    public QuestionType type() {
        return type;
    }

    public String title() {
        return title;
    }

    @Nullable public String description() {
        return description;
    }

    public String descriptionPlacement() {
        return descriptionPlacement;
    }

    public List<Answer> answerList() {
        return answerList;
    }

    public String sendText() {
        return sendText;
    }

    public Node nextMap() {
        return nextMap;
    }

    public String npsMinLabel() {
        return npsMinLabel;
    }

    public String npsMaxLabel() {
        return npsMaxLabel;
    }

    public String cname() {
        return cname;
    }

    public boolean enableRandom() {
        return !disableRandom;
    }

    public boolean anchorLast() {
        return anchorLast;
    }

    public int anchorLastCount() {
        return anchorLastCount;
    }

    public boolean alwaysShowSend() {
        return alwaysShowSend;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public int minAnswersCount() {
        return minAnswersCount;
    }

    public int maxAnswersCount() {
        return maxAnswersCount;
    }

    public String fontStyleQuestion() {
        return fontStyleQuestion;
    }

    public String fontStyleDescription() { return fontStyleDescription; }

    public Question copy(List<Answer> answerList) {
        return new Question(id, type, title, description, descriptionPlacement, answerList, sendText, nextMap,
                            npsMinLabel, npsMaxLabel, cname, disableRandom, anchorLast, anchorLastCount, alwaysShowSend,
                            isRequired, minAnswersCount, maxAnswersCount, fontStyleQuestion, fontStyleDescription);
    }

    public Question copy(String title, String description) {
        return new Question(id, type, title, description, descriptionPlacement, answerList, sendText, nextMap,
                            npsMinLabel, npsMaxLabel, cname, disableRandom, anchorLast, anchorLastCount, alwaysShowSend,
                            isRequired, minAnswersCount, maxAnswersCount, fontStyleQuestion, fontStyleDescription);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        return id == question.id;
    }

    @Override public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override public String toString() {
        return "Question{" +
                "id=" + id +
                '}';
    }
}
