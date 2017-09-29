package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class QuestionTypeDeserializer implements JsonDeserializer<QuestionType> {
    @Override public QuestionType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return QuestionType.from(json.getAsString());
        }
        return QuestionType.UNKNOWN;
    }
}
