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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri

/**
 * Default implementation of the communication layer that uses the [UTransport].
 *
 * @param transport The transport to use for sending the RPC requests
 */
class UClient(
    private val transport: UTransport,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RpcServer, Notifier, Publisher, RpcClient {
    private val rpcServer: InMemoryRpcServer = InMemoryRpcServer(transport)
    private val publisher: SimplePublisher = SimplePublisher(transport)
    private val notifier: SimpleNotifier = SimpleNotifier(transport)
    private val rpcClient: InMemoryRpcClient = InMemoryRpcClient(transport, dispatcher)

    override suspend fun notify(topic: UUri, destination: UUri, options: CallOptions, payload: UPayload?): UStatus {
        return notifier.notify(topic, destination, options, payload)
    }

    override suspend fun registerNotificationListener(topic: UUri, listener: UListener): UStatus {
        return notifier.registerNotificationListener(topic, listener)
    }

    override suspend fun unregisterNotificationListener(topic: UUri, listener: UListener): UStatus {
        return notifier.unregisterNotificationListener(topic, listener)
    }


    override suspend fun publish(topic: UUri, options: CallOptions, payload: UPayload?): UStatus {
        return publisher.publish(topic, options, payload)
    }


    override suspend fun registerRequestHandler(method: UUri, handler: RequestHandler): UStatus {
        return rpcServer.registerRequestHandler(method, handler)
    }


    override suspend fun unregisterRequestHandler(method: UUri, handler: RequestHandler): UStatus {
        return rpcServer.unregisterRequestHandler(method, handler)
    }


    override suspend fun invokeMethod(
        methodUri: UUri,
        requestPayload: UPayload,
        options: CallOptions
    ): Result<UPayload> {
        return rpcClient.invokeMethod(methodUri, requestPayload, options)
    }

    fun close() {
        rpcClient.close()
    }
}