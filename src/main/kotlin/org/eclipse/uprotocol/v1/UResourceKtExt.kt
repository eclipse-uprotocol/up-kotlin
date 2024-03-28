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

import org.eclipse.uprotocol.UServiceTopic


/**
 * The minimum topic ID, below this value are methods.
 */
val MIN_TOPIC_ID: Int
    @JvmSynthetic get() = 0x8000

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
    when {
        id >= MIN_TOPIC_ID -> this@from.id = id
        id > 0 -> forRpcRequest(id = id)
        id == 0 -> forRpcResponse()
        else -> {}
    }
}

/**
 * Build a UResource from a UServiceTopic that is defined in protos and
 * available from generated stubs.
 * @param topic The UServiceTopic to build the UResource from.
 * @return Returns a UResource for an RPC request.
 */
@JvmSynthetic
fun UResourceKt.Dsl.from(topic: UServiceTopic) {
    val nameAndInstanceParts = topic.name.split(".").dropLastWhile { it.isEmpty() }
    val resourceInstance = if (nameAndInstanceParts.size > 1) nameAndInstanceParts[1] else null
    name = nameAndInstanceParts[0]
    id = topic.id
    message = topic.message
    resourceInstance?.let { instance = it }
}
