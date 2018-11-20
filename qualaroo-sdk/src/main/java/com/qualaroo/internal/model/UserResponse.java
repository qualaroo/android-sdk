/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    private final long questionId;
    private final List<Entry> entries;

    UserResponse(long questionId, List<Entry> entries) {
        this.questionId = questionId;
        this.entries = entries;
    }

    public long questionId() {
        return questionId;
    }

    public List<Entry> entries() {
        return entries;
    }

    public static class Builder {

        private final long questionId;
        private final List<Entry> entries = new ArrayList<>();

        public Builder(long questionId) {
            this.questionId = questionId;
        }

        public Builder addTextAnswer(String text) {
            entries.add(Entry.textOnly(text));
            return this;
        }

        public Builder addChoiceAnswer(long answerId) {
            entries.add(Entry.choice(answerId));
            return this;
        }

        public Builder addChoiceAnswerWithComment(long answerId, String comment) {
            entries.add(Entry.choiceWithComment(answerId, comment));
            return this;
        }

        public UserResponse build() {
            return new UserResponse(questionId, entries);
        }
    }

    public static class Entry {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({TYPE_TEXT, TYPE_CHOICE, TYPE_CHOICE_WITH_COMMENT})
        @interface Type {
        }

        public static final int TYPE_TEXT = 1;
        public static final int TYPE_CHOICE = 2;
        public static final int TYPE_CHOICE_WITH_COMMENT = 3;

        private final Long answerId;
        private final String text;

        Entry(Long answerId, String text) {
            this.answerId = answerId;
            this.text = text;
        }

        static Entry textOnly(String text) {
            return new Entry(null, text);
        }

        static Entry choice(long answerId) {
            return new Entry(answerId, null);
        }

        static Entry choiceWithComment(long answerId, String comment) {
            return new Entry(answerId, comment);
        }

        @Type
        public int type() {
            if (answerId == null) {
                return TYPE_TEXT;
            }

            if (text != null) {
                return TYPE_CHOICE_WITH_COMMENT;
            }

            return TYPE_CHOICE;
        }

        public Long answerId() {
            return answerId;
        }

        public String text() {
            return text;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (answerId != null ? !answerId.equals(entry.answerId) : entry.answerId != null) return false;
            return text != null ? text.equals(entry.text) : entry.text == null;
        }

        @Override public int hashCode() {
            int result = answerId != null ? answerId.hashCode() : 0;
            result = 31 * result + (text != null ? text.hashCode() : 0);
            return result;
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserResponse that = (UserResponse) o;

        if (questionId != that.questionId) return false;
        return entries != null ? entries.equals(that.entries) : that.entries == null;
    }

    @Override public int hashCode() {
        int result = (int) (questionId ^ (questionId >>> 32));
        result = 31 * result + (entries != null ? entries.hashCode() : 0);
        return result;
    }

}
