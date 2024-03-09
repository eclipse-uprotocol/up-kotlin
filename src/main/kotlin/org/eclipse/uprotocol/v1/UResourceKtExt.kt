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

import com.google.protobuf.ProtocolMessageEnum

private val MAX_RPC_ID: Int
    @JvmSynthetic get() = 1000

/**
 * Initializes a UResource for an RPC response.
 */
@JvmSynthetic
fun UResourceKt.Dsl.forRpcResponse() {
    name = "rpc"
    instance = "response"
    id = 0
}

/**
 * Initializes a UResource for an RPC request with an ID and/or a method name
 * @param method The method to be invoked.
 * @param id The ID of the request.
 */
@JvmSynthetic
fun UResourceKt.Dsl.forRpcRequest(method: String = instance, id: Int = this@forRpcRequest.id) {
    name = "rpc"
    if (method != instance) {
        instance = method
    }
    if (id != this@forRpcRequest.id) {
        this@forRpcRequest.id = id
    }
}

/**
 * Initializes a UResource from an ID. This method will determine if
 * the id is an RPC or topic ID based on the range
 * @param id The ID of the request.
 */
@JvmSynthetic
fun UResourceKt.Dsl.from(id: Int) {
    if (id < MAX_RPC_ID) forRpcRequest(id = id) else this@from.id = id
}

/**
 * Initializes a UResource from a protobuf message. This method will determine if
 * the message is a RPC or topic message based on the message type
 * @param message The protobuf message.
 */
@JvmSynthetic
fun UResourceKt.Dsl.from(message: ProtocolMessageEnum) {
        message.descriptorForType.containingType.name?.let { name = it }
        message.valueDescriptor.name?.let { instance = it }
        id = message.number
}
