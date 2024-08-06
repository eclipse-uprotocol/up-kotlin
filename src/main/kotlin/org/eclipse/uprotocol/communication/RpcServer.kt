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

import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri

/**
 * Communication Layer (uP-L2) Rpc Server interface.<br></br>
 *
 * This interface provides APIs that services can call to register handlers for
 * incoming requests for given methods.
 */
interface RpcServer {
    /**
     * Register a handler that will be invoked when requests come in from clients for the given method.
     *
     *
     * Note: Only one handler is allowed to be registered per method URI.
     *
     * @param method Uri for the method to register the listener for.
     * @param handler The handler that will process the request for the client.
     * @return Returns the status of registering the RpcListener.
     */
    suspend fun registerRequestHandler(method: UUri, handler: RequestHandler): UStatus


    /**
     * Unregister a handler that will be invoked when requests come in from clients for the given method.
     *
     * @param method Resolved UUri for where the listener was registered to receive messages from.
     * @param handler The handler for processing requests
     * @return Returns status of registering the RpcListener.
     */
    suspend fun unregisterRequestHandler(method: UUri, handler: RequestHandler): UStatus
}