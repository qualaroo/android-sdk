/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.storage.LocalStorage;

import java.util.List;
import java.util.Random;

public class AbTestGroupPercentageProvider {

    private final LocalStorage localStorage;
    private final Random random;

    public AbTestGroupPercentageProvider(LocalStorage localStorage, Random random) {
        this.localStorage = localStorage;
        this.random = random;
    }

    public int abTestGroupPercent(List<Survey> surveys) {
        Integer abTestGroupPercent = localStorage.getAbTestGroupPercent(surveys);
        if (abTestGroupPercent == null) {
            abTestGroupPercent = random.nextInt(100);
            localStorage.storeAbTestGroupPercent(surveys, abTestGroupPercent);
        }
        return abTestGroupPercent;
    }
}
