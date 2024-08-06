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

import org.eclipse.uprotocol.v1.UUri

/**
 * RpcClient is an interface used by code generators for uProtocol services defined in proto files such as
 * the core uProtocol services found in https://github.com/eclipse-uprotocol/uprotocol-core-api. the interface
 * provides a clean contract for mapping a RPC request to a response. Each
 * platform MUST implement this interface. For more details please refer to
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/up-l2/README.adoc[RpcClient Specifications]
 */
interface RpcClient {
    /**
     * suspend API for clients to invoke a method (send an RPC request) and receive the response (the returned
     * [UPayload].
     * Client will set method to be the URI of the method they want to invoke,
     * payload to the request message, and attributes with the various metadata for the
     * method invocation.
     * @param methodUri The method URI to be invoked, ex (long form): /example.hello_world/1/rpc.SayHello.
     * @param requestPayload The request message to be sent to the server.
     * @param options RPC method invocation call options, see [CallOptions]
     * @return Returns the response message or exception with the failure reason as UStatus, wrapped in [Result].
     */
    suspend fun invokeMethod(
        methodUri: UUri,
        requestPayload: UPayload,
        options: CallOptions = CallOptions()
    ): Result<UPayload>
}
