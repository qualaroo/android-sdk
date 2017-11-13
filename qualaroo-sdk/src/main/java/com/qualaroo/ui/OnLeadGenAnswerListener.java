package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface OnLeadGenAnswerListener {
    void onLeadGenAnswered(Map<Long, String> questionIdsWithAnswers);
}
