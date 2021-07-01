package com.qualaroo.internal.model;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.io.Serializable;
import java.util.Locale;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

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
