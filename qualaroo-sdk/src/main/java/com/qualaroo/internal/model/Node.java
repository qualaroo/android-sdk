package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Node implements Serializable {
    private int id;
    private String nodeType;

    public int id() {
        return id;
    }

    public String nodeType() {
        return nodeType;
    }

    @VisibleForTesting Node(int id, String nodeType) {
        this.id = id;
        this.nodeType = nodeType;
    }

    Node() {}
}
