package com.qualaroo.internal.model;


import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Question implements Serializable {
    private long id;
    private QuestionType type;
    private String title;
    private String description;
    private List<Answer> answerList;
    private String sendText;
    private Node nextMap;
    private String npsMinLabel;
    private String npsMaxLabel;
    private String cname;
    private boolean disableRandom;
    private boolean anchorLast;
    private int anchorLastCount;
    private boolean alwaysShowSend;
    private boolean isRequired;

    @VisibleForTesting Question(long id, QuestionType type, String title, String description, List<Answer> answerList, String sendText, Node nextMap, String npsMinLabel, String npsMaxLabel, String cname, boolean disableRandom, boolean anchorLast, int anchorLastCount, boolean alwaysShowSend, boolean isRequired) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
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
    }

    Question() {
        //deserializing with gson requires a default constructor
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

    public String description() {
        return description;
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

    //made this flag a lot more readable in code
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

    public Question mutateWith(List<Answer> answerList) {
        Question mutated = new Question();
        mutated.id = id;
        mutated.type = type;
        mutated.title = title;
        mutated.description = description;
        mutated.answerList = answerList;
        mutated.sendText = sendText;
        mutated.nextMap = nextMap;
        mutated.npsMinLabel = npsMinLabel;
        mutated.npsMaxLabel = npsMaxLabel;
        mutated.disableRandom = disableRandom;
        mutated.anchorLast = anchorLast;
        mutated.alwaysShowSend = alwaysShowSend;
        mutated.isRequired = isRequired;
        mutated.cname = cname;
        mutated.anchorLastCount = anchorLastCount;
        return mutated;
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
