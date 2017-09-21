package com.qualaroo.internal.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public final class LanguageJsonDeserializer implements JsonDeserializer<Language> {

    @Override public Language deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return new Language(json.getAsString());
        }
        return Language.UNKNOWN;
    }

}
