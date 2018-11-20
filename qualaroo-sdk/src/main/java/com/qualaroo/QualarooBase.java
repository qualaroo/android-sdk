/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.LocalStorage;

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class QualarooBase {
    abstract LocalStorage localStorage();
    abstract RestClient restClient();
    abstract SurveysRepository surveysRepository();
    abstract ImageProvider imageProvider();
}
