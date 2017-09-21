package com.qualaroo.internal.model;

import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

public final class Message implements Serializable {
    private int id;
    private String description;

    @VisibleForTesting Message(int id, String description) {
        this.id = id;
        this.description = description;
    }

    Message() {

    }

    public int id() {
        return id;
    }

    public String description() {
        return description;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id == message.id;
    }

    @Override public int hashCode() {
        return id;
    }
}
