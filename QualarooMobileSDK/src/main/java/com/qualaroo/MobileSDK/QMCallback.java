package com.qualaroo.MobileSDK;

import com.qualaroo.MobileSDK.QMState;
import com.qualaroo.MobileSDK.QMReport;

/**
 * Created by Artem on 10.02.17.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public interface QMCallback {
    void callback(QMState state, QMReport report);
}
