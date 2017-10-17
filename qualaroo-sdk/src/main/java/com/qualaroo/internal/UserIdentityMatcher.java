package com.qualaroo.internal;

import com.qualaroo.internal.model.Survey;

public class UserIdentityMatcher {

    private static final String TARGET_TYPE_KNOWN = "yes";
    private static final String TARGET_TYPE_UNKNOWN = "no";
    private static final String TARGET_TYPE_ANY = "any";

    private final UserInfo userInfo;

    public UserIdentityMatcher(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean shouldShow(Survey survey) {
        String targetType = survey.spec().requireMap().wantUserStr();
        if (targetType == null || targetType.equals(TARGET_TYPE_ANY)) {
            return true;
        }
        switch (targetType) {
            case TARGET_TYPE_KNOWN:
                return userInfo.getUserId() != null;
            case TARGET_TYPE_UNKNOWN:
                return userInfo.getUserId() == null;
            default:
                return true;
        }
    }
}
