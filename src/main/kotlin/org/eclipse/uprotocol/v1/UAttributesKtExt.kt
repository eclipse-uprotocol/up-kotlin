/*
 * Copyright (c) 2024 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.v1

import org.eclipse.uprotocol.uuid.factory.UUIDV8

/**
 * Construct a UAttributesBuilder for a publish message.
 * @param source   Source address of the message.
 * @param priority The priority of the message.
 * @return Returns the UAttributesBuilder with the configured source and priority.
 */
@JvmSynthetic
fun UAttributesKt.Dsl.forPublication(source: UUri, priority: UPriority) {
    this@forPublication.source = source
    this@forPublication.priority = priority
    id = UUIDV8()
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
fun UAttributesKt.Dsl.forNotification(source: UUri, sink: UUri, priority: UPriority) {
    this@forNotification.source = source
    this@forNotification.sink = sink
    this@forNotification.priority = priority
    id = UUIDV8()
    type = UMessageType.UMESSAGE_TYPE_PUBLISH
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
fun UAttributesKt.Dsl.forRequest(source: UUri, sink: UUri, priority: UPriority, ttl: Int) {
    this@forRequest.source = source
    this@forRequest.sink = sink
    this@forRequest.priority = priority
    this@forRequest.ttl = ttl
    id = UUIDV8()
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
fun UAttributesKt.Dsl.forResponse(source: UUri, sink: UUri, priority: UPriority, reqId: UUID) {
    this@forResponse.source = source
    this@forResponse.sink = sink
    this@forResponse.priority = priority
    id = UUIDV8()
    type = UMessageType.UMESSAGE_TYPE_RESPONSE
    reqid = reqId
}