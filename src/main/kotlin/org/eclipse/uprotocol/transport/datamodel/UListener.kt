/*
 * Copyright (c) 2023 General Motors GTO LLC
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

package org.eclipse.uprotocol.transport.datamodel

import org.eclipse.uprotocol.v1.UAttributes
import org.eclipse.uprotocol.v1.UUri

/**
 * For any implementation that defines some kind of callback or function that will be called to handle incoming messages.
 */
interface UListener {
    /**
     * Method called to handle/process events.
     * @param topic Topic the underlying source of the message.
     * @param payload Payload of the message.
     * @param attributes Transportation attributes
     * @return Returns an Ack every time a message is received and processed.
     */
    fun onReceive(topic: UUri, payload: UPayload, attributes: UAttributes): UStatus
}
