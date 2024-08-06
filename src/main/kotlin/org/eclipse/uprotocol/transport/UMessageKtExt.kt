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

package org.eclipse.uprotocol.transport

import org.eclipse.uprotocol.communication.UPayload
import org.eclipse.uprotocol.transport.validator.Request
import org.eclipse.uprotocol.uri.validator.isRpcMethod
import org.eclipse.uprotocol.uri.validator.isRpcResponse
import org.eclipse.uprotocol.uri.validator.isTopic
import org.eclipse.uprotocol.uuid.validate.UUIDV7Validator
import org.eclipse.uprotocol.v1.*

/**
 * Set the Priority of a UMessage. The priority should be at least CS4 for request and response messages,
 * and CS1 for other messages, incorrect priority will be ignored.
 * @param priority The priority of the message.
 *
 * @return Returns the UMessage with the configured priority.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setPriority(priority: UPriority) {
    val basePriority = when (attributes.type) {
        UMessageType.UMESSAGE_TYPE_REQUEST, UMessageType.UMESSAGE_TYPE_RESPONSE -> UPriority.UPRIORITY_CS4

        else -> UPriority.UPRIORITY_CS1
    }
    if (priority.number >= basePriority.number) {
        attributes = attributes.copy { this.priority = priority }
    }
}

/**
 * Set the Permission Level of a UMessage.
 * @param permissionLevel The permission level of the message.
 *
 * @return Returns the UMessage with the configured Permission Level.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setPermissionLevel(permissionLevel: Int) {
    attributes = attributes.copy { this.permissionLevel = permissionLevel }
}

/**
 * Set the ttl of a UMessage.
 * @param ttl The time-to-live of the message.
 *
 * @return Returns the UMessage with the configured ttl.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setTtl(ttl: Int) {
    attributes = attributes.copy { this.ttl = ttl }
}

/**
 * Set the commstatus of a UMessage.
 * @param commstatus The Communication Status of the message.
 *
 * @return Returns the UMessage with the configured commstatus.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setCommStatus(commstatus: UCode) {
    attributes = attributes.copy { this.commstatus = commstatus }
}

/**
 * Set the reqid of a UMessage.
 * @param reqid The Required ID of the message.
 *
 * @return Returns the UMessage with the configured reqid.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setReqid(reqid: UUID) {
    attributes = attributes.copy { this.reqid = reqid }
}

/**
 * Set the token of a UMessage.
 * @param token The token of the message.
 *
 * @return Returns the UMessage with the configured token.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setToken(token: String) {
    attributes = attributes.copy { this.token = token }
}

/**
 * Set the traceparent of a UMessage.
 * @param traceparent The traceparent of the message.
 *
 * @return Returns the UMessage with the configured traceparent.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setTraceparent(traceparent: String) {
    attributes = attributes.copy { this.traceparent = traceparent }
}

/**
 * Build a message with the passed {@link UPayload}.
 *
 * @param payload The payload to be packed into the message.
 * @return Returns the UMessage with the configured payload.
 */
@JvmSynthetic
fun UMessageKt.Dsl.setPayload(payload: UPayload) {
    this.payload = payload.data
    attributes = attributes.copy { payloadFormat = payload.format }
}

/**
 * Construct a UMessageBuilder for a publish message.
 * @param source The topic the message is published to (a.k.a. Source address).
 *
 * @return Returns the UMessage with the configured payload.
 */
@JvmSynthetic
fun UMessageKt.Dsl.forPublication(source: UUri) {
    require(source.isTopic()) { "The source must be a topic" }
    attributes = uAttributes {
        forPublication(source, UPriority.UPRIORITY_CS1)
    }
}

/**
 * Construct a UMessageBuilder for a notification message.
 * @param source The topic the message is published to (a.k.a. Source address).
 * @param sink The destination of the client that send the request.
 * @return Returns the UMessage with the configured payload.
 */
@JvmSynthetic
fun UMessageKt.Dsl.forNotification(source: UUri, sink: UUri) {
    require(source.isTopic() && sink.isRpcResponse()) { "The source must be a topic and sink must be a response." }
    attributes = uAttributes {
        forNotification(source, sink, UPriority.UPRIORITY_CS1)
    }
}

/**
 * Construct a UMessageBuilder for a request message.
 * @param source Source address for the message (address of the client sending the request message).
 * @param sink The method that is being requested (a.k.a. destination address).
 * @param ttl The time to live in milliseconds.
 * @return Returns the UMessage with the configured payload.
 */
@JvmSynthetic
fun UMessageKt.Dsl.forRequest(source: UUri, sink: UUri, ttl: Int) {
    // Validate the source and sink
    require(sink.isRpcMethod() && source.isRpcResponse()) {
        "source must be an rpc method and sink must be a request."
    }
    // Validate the ttl
    require(ttl > 0) { "ttl must be greater than 0." }
    attributes = uAttributes {
        forRequest(source, sink, UPriority.UPRIORITY_CS4, ttl)
    }
}

/**
 * Construct a UMessageBuilder for a response message.
 * @param source The source address of the method that was requested
 * @param sink The destination of the client that send the request.
 * @param reqId The original request UUID used to correlate the response to the request.
 * @return Returns the UMessage with the configured payload.
 */
@JvmSynthetic
fun UMessageKt.Dsl.forResponse(source: UUri, sink: UUri, reqId: UUID) {
    // Validate the source and sink
    require(sink.isRpcResponse() && source.isRpcMethod()) {
        "sink must be a response and source must be an rpc method."
    }
    require(UUIDV7Validator.validate(reqId).getCode() === UCode.OK) { "reqid is not a valid UUID." }
    attributes = uAttributes {
        forResponse(source, sink, UPriority.UPRIORITY_CS4, reqId)
    }
}

/**
 * Construct a UMessageBuilder for a response message.
 * @param request The request message that was received.
 * @return Returns the UMessage with the configured payload.
 */
@JvmSynthetic
fun UMessageKt.Dsl.forResponse(request: UAttributes) {
    // Validate the request
    require(!Request.validate(request).isFailure()) { "request must contain valid request attributes." }
    attributes = uAttributes {
        forResponse(request)
    }
}
