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

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.uprotocol.transport.*
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.v1.UUID

/**
 * The following is an example implementation of the [RpcClient] interface that
 * wraps the [UTransport] for implementing the RPC pattern to send
 * RPC requests and receive RPC responses. This implementation uses an in-memory
 * map to store the futures that needs to be completed when the response comes in from the server.
 *
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 * or directly use the [UTransport] to send RPC requests and register listeners that
 * handle the RPC responses.
 *
 * @param transport the transport to use for sending the RPC requests
 */
class InMemoryRpcClient(
    private val transport: UTransport,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RpcClient {
    // Map to store the futures that needs to be completed when the response comes in
    private val mRequests = HashMap<UUID, CompletableDeferred<UMessage>>()

    // Generic listener to handle all RPC response messages
    private val mResponseHandler = UListener { response: UMessage ->
        this.handleResponses(response)
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val mutex = Mutex()

    init {
        scope.launch {
            transport.registerListener(
                UUriFactory.ANY,
                transport.getSource(), mResponseHandler
            )
        }
    }


    /**
     * Invoke a method (send an RPC request) and receive the response
     * the returned [UPayload] wrapped in [Result].
     *
     * @param methodUri The method URI to be invoked.
     * @param requestPayload The request message to be sent to the server.
     * @param options RPC method invocation call options, see [CallOptions]
     * @return Returns the [Result] with the response [UPayload] or exception with the failure
     * reason as [UStatus].
     */
    override suspend fun invokeMethod(
        methodUri: UUri,
        requestPayload: UPayload,
        options: CallOptions
    ): Result<UPayload> {
        try {
            val request = uMessage {
                forRequest(transport.getSource(), methodUri, options.timeout)
                if (options.token.isNotBlank()) {
                    setToken(options.token)
                }
                setPayload(requestPayload)
            }
            transport.send(request).takeIf { it.code != UCode.OK }?.let {
                throw UStatusException(it)
            }
            val result = withTimeout(request.attributes.ttl.toLong()) {
                mutex.withLock {
                    val currentRequest = mRequests[request.attributes.id]
                    if (currentRequest != null) {
                        throw UStatusException(UCode.ALREADY_EXISTS, "Duplicated request found")
                    }
                    val response = CompletableDeferred<UMessage>()
                    mRequests[request.attributes.id] = response
                    response
                }.await()
            }
            return Result.success(UPayload.pack(result.payload, result.attributes.payloadFormat))
        } catch (e: Exception) {
            return when (e) {
                is UStatusException -> {
                    Result.failure(e)
                }

                is TimeoutCancellationException -> {
                    Result.failure(UStatusException(UCode.DEADLINE_EXCEEDED, "Request timed out"))
                }

                else -> {
                    Result.failure(UStatusException(UCode.UNKNOWN, e.message))
                }
            }
        }
    }

    fun close() {
        mRequests.clear()
        scope.launch {
            transport.unregisterListener(UUriFactory.ANY, transport.getSource(), mResponseHandler)
        }
    }

    /**
     * Handle the responses coming back from the server
     * @param response The response message from the server
     */
    private suspend fun handleResponses(response: UMessage) {
        // Only handle responses messages, ignore all other messages like notifications
        if (response.attributes.type != UMessageType.UMESSAGE_TYPE_RESPONSE) {
            return
        }
        // Check if the response is for a request we made, if not then ignore it
        val responseDeferred = mutex.withLock { mRequests.remove(response.attributes.reqid) } ?: return
        // Check if the response has a commstatus and if it is not OK then complete the future with an exception
        if (response.attributes.hasCommstatus()) {
            val code = response.attributes.commstatus
            responseDeferred.completeExceptionally(UStatusException(code, "Communication error [$code]"))
        } else {
            responseDeferred.complete(response)
        }
    }
}