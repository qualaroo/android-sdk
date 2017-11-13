package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Node implements Serializable {
    private long id;
    private String nodeType;

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

    Node() {}
}
