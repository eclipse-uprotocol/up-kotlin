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
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.transport

import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UMessage
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri


/**
 * UTransport is the uP-L1 interface that provides a common API for uE developers to send and receive messages.
 * UTransport implementations contain the details for connecting to the underlying transport technology and
 * sending UMessage using the configured technology. For more information please refer to
 * https://github.com/eclipse-uprotocol/up-spec/blob/main/up-l1/README.adoc.
 */
interface UTransport {

    /**
     * Send a message over the transport.
     * @param message the [UMessage] to be sent.
     * @return Returns [UStatus] with [UCode] set to the status code (successful or failure).
     */
    fun send(message: UMessage): UStatus

    /**
     * Register `UListener` for `UUri` topic to be called when a message is received.
     * @param topic `UUri` to listen for messages from.
     * @param listener The `UListener` that will be executed when the message is
     * received on the given `UUri`.
     * @return Returns [UStatus] with [UCode.OK] if the listener is registered
     * correctly, otherwise it returns with dthe appropriate failure.
     */
    fun registerListener(topic: UUri, listener: UListener): UStatus

    /**
     * Unregister `UListener` for `UUri` topic. Messages arriving on this topic will
     * no longer be processed by this listener.
     * @param topic `UUri` to the listener was registered for.
     * @param listener The `UListener` that will no longer want to be registered to receive
     * messages.
     * @return Returns [UStatus] with [UCode.OK] if the listener is unregistered
     * correctly, otherwise it returns with the appropriate failure.
     */
    fun unregisterListener(topic: UUri, listener: UListener): UStatus
}