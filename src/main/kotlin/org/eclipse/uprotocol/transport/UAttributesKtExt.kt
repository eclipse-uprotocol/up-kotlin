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

import org.eclipse.uprotocol.uuid.factory.UUIDV7
import org.eclipse.uprotocol.v1.*


/**
 * Construct a UMessageBuilder for a Publish message.
 * @param source   Source address of the message.
 * @param priority The priority of the message.
 * @return Returns the UAttributesBuilder with the configured source and priority.
 */
@JvmSynthetic
internal fun UAttributesKt.Dsl.forPublication(source: UUri, priority: UPriority) {
    this@forPublication.source = source
    this@forPublication.priority = priority
    id = UUIDV7()
    type = UMessageType.UMESSAGE_TYPE_PUBLISH
}

/**
 * Construct a UAttributesBuilder for a notification message.
 * @param source   Source address of the message.
 * @param sink The destination URI.
 * @param priority The priority of the message.
 * @return Returns the UAttributesBuilder with the configured source, sink and priority.
 */
@JvmSynthetic
internal fun UAttributesKt.Dsl.forNotification(source: UUri, sink: UUri, priority: UPriority) {
    this@forNotification.source = source
    this@forNotification.sink = sink
    this@forNotification.priority = priority
    id = UUIDV7()
    type = UMessageType.UMESSAGE_TYPE_NOTIFICATION
}

/**
 * Construct a UAttributesBuilder for a request message.
 * @param source   Source address of the message.
 * @param sink The destination URI.
 * @param priority The priority of the message.
 * @param ttl The time to live in milliseconds.
 * @return Returns the UAttributesBuilder with the configured source, sink, priority and ttl.
 */
@JvmSynthetic
internal fun UAttributesKt.Dsl.forRequest(source: UUri, sink: UUri, priority: UPriority, ttl: Int) {
    this@forRequest.source = source
    this@forRequest.sink = sink
    this@forRequest.priority = priority
    this@forRequest.ttl = ttl
    id = UUIDV7()
    type = UMessageType.UMESSAGE_TYPE_REQUEST
}

/**
 * Construct a UAttributesBuilder for a response message.
 * @param source   Source address of the message.
 * @param sink The destination URI.
 * @param priority The priority of the message.
 * @param reqId The original request UUID used to correlate the response to the request.
 * @return Returns the UAttributesBuilder with the configured source, sink, priority and reqid.
 */
@JvmSynthetic
internal fun UAttributesKt.Dsl.forResponse(source: UUri, sink: UUri, priority: UPriority, reqId: UUID) {
    this@forResponse.source = source
    this@forResponse.sink = sink
    this@forResponse.priority = priority
    id = UUIDV7()
    type = UMessageType.UMESSAGE_TYPE_RESPONSE
    reqid = reqId
}

/**
 * Construct a UAttributesBuilder for a response message.
 * @param request The original request {@code UAttributes} used to correlate the response to the request.
 * @return Returns the UAttributesBuilder with the configured source, sink, priority and reqid.
 */
@JvmSynthetic
internal fun UAttributesKt.Dsl.forResponse(request: UAttributes) {
    source = request.sink
    sink = request.source
    priority = request.priority
    id = UUIDV7()
    type = UMessageType.UMESSAGE_TYPE_RESPONSE
    reqid = request.id
}