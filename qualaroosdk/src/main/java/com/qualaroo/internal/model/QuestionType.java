package com.qualaroo.internal.model;

public enum QuestionType {
    NPS("nps"),
    RADIO("radio"),
    CHECKBOX("checkbox"),
    TEXT("text"),
    DROPDOWN("dropdown"),
    UNKNOWN("-1");

    private final String value;

    static QuestionType from(String value) {
        for (QuestionType questionType : values()) {
            if (questionType.value.equals(value)) {
                return questionType;
            }
        }
        return UNKNOWN;
    }

    QuestionType(String value) {
        this.value = value;
    }
}
