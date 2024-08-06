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

import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.*
import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.*
import org.eclipse.uprotocol.transport.validator.UAttributesValidator.Companion.getValidator
import org.eclipse.uprotocol.v1.*

/**
 * TestUTransport is a test implementation of the UTransport interface
 * that can only hold a single listener for testing.
 */
open class TestUTransport(
    private val mSource: UUri = uUri {
        authorityName = "Hartley"
        ueId = 4
        ueVersionMajor = 1
    },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UTransport {
    val listeners: MutableList<UListener> = mutableListOf()
    protected val scope = CoroutineScope(SupervisorJob() + dispatcher)
    var lastMessage: UMessage = UMessage.getDefaultInstance()
        private set

    protected var sendJob : Job? = null
    open fun buildResponse(request: UMessage): UMessage {
        // If the request is a subscribe or unsubscribe request, return the appropriate response
        return if (request.attributes.sink.ueId == 0) {
            if (request.attributes.sink.resourceId == 1) {
                try {
                    val subscriptionRequest = SubscriptionRequest.parseFrom(request.payload)
                    val subResponse = subscriptionResponse {
                        topic = subscriptionRequest.topic
                        status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED }
                    }
                    uMessage {
                        forResponse(request.attributes)
                        setPayload(UPayload.pack(subResponse))
                    }
                } catch (e: InvalidProtocolBufferException) {
                    uMessage {
                        forResponse(request.attributes)
                        setPayload(UPayload.pack(unsubscribeResponse { }))
                    }
                }
            } else {
                uMessage {
                    forResponse(request.attributes)
                    setPayload(UPayload.pack(unsubscribeResponse { }))
                }
            }
        } else {
            uMessage {
                forResponse(request.attributes)
                setReqid(request.attributes.id)
                setPayload(UPayload.pack(request.payload, request.attributes.payloadFormat))
            }
        }
    }

    override suspend fun send(message: UMessage): UStatus {
        lastMessage = message
        val validator = message.attributes.getValidator()

        if (validator.validate(message.attributes).isFailure()) {
            println("flag1")
            return uStatus {
                code = UCode.INVALID_ARGUMENT
                this.message = "Invalid message attributes"
            }
        }

        if (message.attributes.type == UMessageType.UMESSAGE_TYPE_REQUEST) {
            val response = buildResponse(message)
            sendJob = scope.launch {
                delay(100)
                listeners.forEach { listener -> listener.onReceive(response) }
            }
        }

        if (message.attributes.type == UMessageType.UMESSAGE_TYPE_NOTIFICATION) {
            sendJob = scope.launch {
                println("flag2")
                listeners.forEach { listener -> listener.onReceive(message) }
            }
        }

        return OK_STATUS
    }

    /*
     * Register a listener based on the source and sink URIs.
     */
    override suspend fun registerListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
        sendJob?.join()
        if (!listeners.contains(listener)){
            listeners.add(listener)
        }
        return OK_STATUS
    }

    override suspend fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
        sendJob?.join()
        return uStatus {
            code = if (listeners.remove(listener)) UCode.OK else UCode.NOT_FOUND
        }
    }

    override fun getSource(): UUri {
        return mSource
    }

    override fun close() {
        sendJob?.cancel()
        listeners.clear()
    }
}


/**
 * Timeout uTransport simply sendS a reply later than the ttl
 */
internal class TimeoutUTransport(dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    TestUTransport(dispatcher = dispatcher){
    override suspend fun send(message: UMessage): UStatus {
        if (message.attributes.type == UMessageType.UMESSAGE_TYPE_REQUEST) {
            val response = buildResponse(message)
            sendJob = scope.launch {
                delay(message.attributes.ttl+10L)
                listeners.forEach { listener -> listener.onReceive(response) }
            }
        }
        return OK_STATUS
    }
}

internal class ErrorUTransport(dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    TestUTransport(dispatcher = dispatcher)  {
    override suspend fun send(message: UMessage): UStatus {
        return uStatus { code = UCode.FAILED_PRECONDITION }
    }

    override suspend fun registerListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
        return uStatus { code = UCode.FAILED_PRECONDITION }
    }

    override suspend fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
        return uStatus { code = UCode.FAILED_PRECONDITION }
    }
}

/**
 * Test UTransport that will set the commstatus for an error
 */
internal class CommStatusTransport(dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    TestUTransport(dispatcher = dispatcher) {
    override fun buildResponse(request: UMessage): UMessage {
        return uMessage {
            forResponse(request.attributes)
            setReqid(request.attributes.id)
            setCommStatus(UCode.DATA_LOSS)
            setPayload(UPayload.pack(request.payload, request.attributes.payloadFormat))
        }
    }
}

/**
 * Test UTransport that will set the commstatus for a success response
 */
internal class CommStatusOkTransport(dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    TestUTransport(dispatcher = dispatcher) {
    override fun buildResponse(request: UMessage): UMessage {
        val status = uStatus {
            code = UCode.OK
            message = "No Communication Error"
        }
        return uMessage {
            forResponse(request.attributes)
            setCommStatus(UCode.OK)
            setPayload(UPayload.pack(status))
        }
    }
}

/**
 * Test UTransport that will set the Response type incorrect
 */
internal class InvalidResponseTransport(dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    TestUTransport(dispatcher = dispatcher) {
    override fun buildResponse(request: UMessage): UMessage {
        return uMessage {
            forResponse(request.attributes)
            setReqid(request.attributes.id)
            setPayload(UPayload.pack(request.payload, request.attributes.payloadFormat))
            attributes = attributes.copy {
                type = UMessageType.UMESSAGE_TYPE_NOTIFICATION
            }
        }
    }
}

internal class EchoUTransport(dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    TestUTransport(dispatcher = dispatcher) {
    val receivedResponse = mutableListOf<UMessage>()
    override fun buildResponse(request: UMessage): UMessage {
        return request
    }

    override suspend fun send(message: UMessage): UStatus {
        if (message.attributes.type == UMessageType.UMESSAGE_TYPE_REQUEST) {
            val response = buildResponse(message)
            sendJob = scope.launch {
                listeners.forEach { listener -> listener.onReceive(response) }
            }
        }

        if (message.attributes.type == UMessageType.UMESSAGE_TYPE_RESPONSE) {
            receivedResponse.add(message)
        }

        if (message.attributes.type == UMessageType.UMESSAGE_TYPE_NOTIFICATION) {
            val response = buildResponse(message)
            sendJob = scope.launch {
                listeners.forEach { listener -> listener.onReceive(response) }
            }
        }

        return OK_STATUS
    }
}

internal val OK_STATUS = uStatus { code = UCode.OK }