package com.qualaroo;

import androidx.annotation.RestrictTo;

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
