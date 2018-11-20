/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.model;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.io.Serializable;
import java.util.Locale;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Language implements Serializable {

    public static final Language UNKNOWN = new Language("");

    private final String value;

    public Language(@NonNull String value) {
        this.value = value.toLowerCase(Locale.ROOT);
    }

    @NonNull
    public String value() {
        return value;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Language language = (Language) o;

        return value != null ? value.equals(language.value) : language.value == null;
    }

    @Override public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
