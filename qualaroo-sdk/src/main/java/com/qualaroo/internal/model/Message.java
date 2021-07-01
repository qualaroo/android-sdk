package com.qualaroo.internal.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Message implements Serializable {
    private final long id;
    private final MessageType type;
    private final String description;
    private final CtaMap ctaMap;

    @VisibleForTesting Message(long id, MessageType type, String description, CtaMap ctaMap) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.ctaMap = ctaMap;
    }

    @SuppressWarnings("unused") private Message() {
        this.id = 0;
        this.type = null;
        this.description = null;
        this.ctaMap = null;
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

    public Message copy(String description) {
        return new Message(id, type, description, ctaMap);
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
        private final String text;
        private final String uri;

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

        @SuppressWarnings("unused") private CtaMap() {
            this.text = null;
            this.uri = null;
        }
    }
}
