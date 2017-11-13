package com.qualaroo.internal.model;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Message implements Serializable {
    private long id;
    private MessageType type;
    private String description;
    private CtaMap ctaMap;

    @VisibleForTesting Message(long id, MessageType type, String description, CtaMap ctaMap) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.ctaMap = ctaMap;
    }

    Message() {

    }

    public long id() {
        return id;
    }

    public String description() {
        return description;
    }

    public MessageType type() {
        return type;
    }

    public CtaMap ctaMap() {
        return ctaMap;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id == message.id;
    }


    @Override public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public static class CtaMap implements Serializable{
        private String text;
        private String uri;

        @Nullable public String text() {
            return text;
        }

        public String uri() {
            return uri;
        }

        @VisibleForTesting CtaMap(String text, String uri) {
            this.text = text;
            this.uri = uri;
        }

        CtaMap() {

        }
    }
}
