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

import org.eclipse.uprotocol.uuid.factory.UuidFactory

/**
 * Construct a UAttributesBuilder for a publish message.
 * @param uPriority The priority of the message.
 * @return Returns the UAttributesBuilder with the configured priority.
 */
@JvmSynthetic
fun UAttributesKt.Dsl.forPublication(uPriority: UPriority) {
    id = UuidFactory.Factories.UPROTOCOL.factory().create()
    type = UMessageType.UMESSAGE_TYPE_PUBLISH
    priority = uPriority
}

/**
 * Construct a UAttributesBuilder for a notification message.
 * @param uPriority The priority of the message.
 * @param notificationSink The destination URI.
 * @return Returns the UAttributesBuilder with the configured priority and sink.
 */
@JvmSynthetic
fun UAttributesKt.Dsl.forNotification(uPriority: UPriority, notificationSink: UUri) {
    id = UuidFactory.Factories.UPROTOCOL.factory().create()
    type = UMessageType.UMESSAGE_TYPE_PUBLISH
    priority = uPriority
    sink = notificationSink
}

/**
 * Construct a UAttributesBuilder for a request message.
 * @param uPriority The priority of the message.
 * @param requestSink The destination URI.
 * @param inTtl The time to live in milliseconds.
 * @return Returns the UAttributesBuilder with the configured priority, sink and ttl.
 */
@JvmSynthetic
fun UAttributesKt.Dsl.forRequest(uPriority: UPriority, requestSink: UUri, inTtl: Int) {
    id = UuidFactory.Factories.UPROTOCOL.factory().create()
    type = UMessageType.UMESSAGE_TYPE_REQUEST
    priority = uPriority
    sink = requestSink
    ttl = inTtl
}

/**
 * Construct a UAttributesBuilder for a response message.
 * @param uPriority The priority of the message.
 * @param responseSink The destination URI.
 * @param reqId The original request UUID used to correlate the response to the request.
 * @return Returns the UAttributesBuilder with the configured priority, sink and reqid.
 */
@JvmSynthetic
fun UAttributesKt.Dsl.forResponse(uPriority: UPriority, responseSink: UUri, reqId: UUID) {
    id = UuidFactory.Factories.UPROTOCOL.factory().create()
    type = UMessageType.UMESSAGE_TYPE_RESPONSE
    priority = uPriority
    sink = responseSink
    reqid = reqId
}