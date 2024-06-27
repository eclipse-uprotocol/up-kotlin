/**
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

import org.eclipse.uprotocol.v1.UMessage

/**
 * RequestListener is used by the RpcServer to handle incoming requests and automatically sends
 * back the response to the client.
 *
 * The service must implement the [.onReceive] method to handle
 * the request and then complete the future passed to the method that triggers the uLink library to
 * send (over the transport) the response.
 *
 */
fun interface RequestHandler {
    /**
     * Method called to handle/process request messages.
     *
     * @param message The request message received.
     * @return the response payload.
     * @throws UStatusException If the service encounters an error processing the request.
     */
    @Throws(UStatusException::class)
    fun handleRequest(message: UMessage): UPayload
}