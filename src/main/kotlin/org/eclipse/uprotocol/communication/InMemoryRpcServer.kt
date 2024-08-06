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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.uprotocol.transport.*
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.*
import java.util.*

/**
 * The following is an example implementation of the [RpcServer] interface that
 * wraps the [UTransport] for implementing the server-side of the RPC pattern
 * to register handlers for processing RPC requests from clients. This implementation
 * uses an in-memory map to store the request handlers that needs to be invoked when the
 * request comes in from the client.
 *
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 * or directly use the [UTransport] to register listeners that handle
 * RPC requests and send RPC responses.
 *
 * @param transport The transport to use for sending the RPC requests.
 */
class InMemoryRpcServer(private val transport: UTransport) : RpcServer {

    // Map to store the request handlers, so we can handle the right request on the server side
    private val mRequestsHandlers = HashMap<UUri, RequestHandler>()

    // Generic listener to handle all RPC request messages
    private val mRequestHandler = UListener { message ->
        handleRequests(message)
    }

    private val handlerLock = Mutex()

    /**
     * Register a handler that will be invoked when requests come in from clients for the given method.
     *
     * Note: Only one handler is allowed to be registered per method URI.
     *
     * @param method Uri for the method to register the listener for.
     * @param handler The handler that will process the request for the client.
     * @return Returns the status of registering the RpcListener.
     */
    override suspend fun registerRequestHandler(method: UUri, handler: RequestHandler): UStatus {
        // Ensure the method URI matches the transport source URI 
        if (method.authorityName != transport.getSource().authorityName ||
            method.ueId != transport.getSource().ueId ||
            method.ueVersionMajor != transport.getSource().ueVersionMajor
        ) {
            return uStatus {
                code = UCode.INVALID_ARGUMENT
                message = "Method URI does not match the transport source URI"
            }
        }
        return handlerLock.withLock {
            mRequestsHandlers[method]?.let {
                uStatus {
                    code = UCode.ALREADY_EXISTS
                    message = "Handler already registered"
                }
            } ?: run {
                val status: UStatus = transport.registerListener(UUriFactory.ANY, method, mRequestHandler)
                if (status.code == UCode.OK) {
                    mRequestsHandlers[method] = handler
                }
                status
            }
        }
    }


    /**
     * Unregister a handler that will be invoked when requests come in from clients for the given method.
     *
     * @param method Resolved UUri for where the listener was registered to receive messages from.
     * @param handler The handler for processing requests
     * @return Returns status of registering the RpcListener.
     */
    override suspend fun unregisterRequestHandler(method: UUri, handler: RequestHandler): UStatus {
        // Ensure the method URI matches the transport source URI
        if (method.authorityName != transport.getSource().authorityName ||
            method.ueId != transport.getSource().ueId ||
            method.ueVersionMajor != transport.getSource().ueVersionMajor
        ) {
            return uStatus {
                code = UCode.INVALID_ARGUMENT
                message = "Method URI does not match the transport source URI"
            }
        }

        val removeResult = handlerLock.withLock { mRequestsHandlers.remove(method, handler) }
        if (removeResult) {
            return transport.unregisterListener(UUriFactory.ANY, method, mRequestHandler)
        }

        return uStatus {
            code = UCode.NOT_FOUND
            message = "Handler not found"
        }
    }


    /**
     * Generic incoming handler to process RPC requests from clients
     * @param request The request message from clients
     */
    private suspend fun handleRequests(request: UMessage) {
        // Only handle request messages, ignore all other messages like notifications
        if (request.attributes.type != UMessageType.UMESSAGE_TYPE_REQUEST) {
            return
        }
        // Check if the request is for one that we have registered a handler for, if not ignore it
        val handler = mRequestsHandlers[request.attributes.sink] ?: return
        transport.send(uMessage {
            forResponse(request.attributes)
            runCatching{
                handler.handleRequest(request)
            }.getOrElse { e ->
                val code = if (e is UStatusException) {
                    e.status.code
                } else {
                    UCode.INTERNAL
                }
                setCommStatus(code)
                null
            }?.let {
                setPayload(it)
            }
        })
    }
}