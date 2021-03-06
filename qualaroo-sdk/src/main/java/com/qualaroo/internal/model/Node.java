package com.qualaroo.internal.model;

import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Node implements Serializable {
    private final long id;
    private final String nodeType;

    public long id() {
        return id;
    }

    public String nodeType() {
        return nodeType;
    }

    @VisibleForTesting Node(long id, String nodeType) {
        this.id = id;
        this.nodeType = nodeType;
    }

    @SuppressWarnings("unused") private Node() {
        this.id = 0;
        this.nodeType = null;
    }
}
