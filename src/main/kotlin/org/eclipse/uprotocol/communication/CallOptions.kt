/*
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication

import org.eclipse.uprotocol.v1.UPriority

/**
 * This class is used to pass metadata to method invocation on the client side.
 * @param timeout How long we should wait for a request to be processed by the server.
 * @param priority The priority of the request, default per the spec is CS4.
 * @param token Token that is used for TAP.
 */
data class CallOptions(
    val timeout: Int = TIMEOUT_DEFAULT,
    val priority: UPriority = UPriority.UPRIORITY_CS4,
    val token: String = ""
) {
    companion object {
        /**
         * Default timeout of 10 seconds (measured in milliseconds).
         */
        const val TIMEOUT_DEFAULT: Int = 10000
    }
}