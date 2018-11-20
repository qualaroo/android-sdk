/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal;

import java.util.UUID;

public class SurveySession {

    private final String uuid = UUID.randomUUID().toString();

    public String uuid() {
        return uuid;
    }
}
