package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MessageTypeDeserializer implements JsonDeserializer<MessageType> {
    @Override public MessageType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return MessageType.from(json.getAsString());
        }
        return MessageType.UNKNOWN;
    }
}
