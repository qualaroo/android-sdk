package com.qualaroo;

import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.LocalStorage;

abstract class QualarooBase {
    abstract LocalStorage localStorage();
    abstract RestClient restClient();
    abstract SurveysRepository surveysRepository();
    abstract ImageProvider imageProvider();
}
