/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.executor;

import java.util.concurrent.Executor;

public class ExecutorSet {

    private final Executor uiThreadExecutor;
    private final Executor dataExecutor;
    private final Executor backgroundExecutor;

    public ExecutorSet(Executor uiThreadExecutor, Executor dataExecutor, Executor backgroundExecutor) {
        this.uiThreadExecutor = uiThreadExecutor;
        this.dataExecutor = dataExecutor;
        this.backgroundExecutor = backgroundExecutor;
    }

    public Executor uiThreadExecutor() {
        return uiThreadExecutor;
    }

    public Executor dataExecutor() {
        return dataExecutor;
    }

    public Executor backgroundExecutor() {
        return backgroundExecutor;
    }
}
