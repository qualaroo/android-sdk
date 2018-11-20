/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.demo.repository

import com.google.gson.annotations.SerializedName

data class Survey(@SerializedName("alias") val alias: String)
