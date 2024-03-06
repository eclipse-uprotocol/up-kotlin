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

package org.eclipse.uprotocol.uri.factory

import com.google.protobuf.ProtocolMessageEnum
import org.eclipse.uprotocol.v1.UResource
import org.eclipse.uprotocol.v1.uResource

object UResourceFactory {
    private const val MAX_RPC_ID = 1000

    /**
     * Create a UResource for an RPC response.
     * @return Returns a UResource for an RPC response.
     */
    fun createForRpcResponse(): UResource {

        return uResource {
            name = "rpc"
            instance = "response"
            id = 0
        }
    }

    /**
     * Create a UResource for an RPC request with at least an ID or a method name
     * @param method The method to be invoked.
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    fun createForRpcRequest(method: String? = null, id: Int? = null): UResource {
        return uResource {
            name = "rpc"
            if (method != null) instance = method
            if (id != null) this.id = id
        }
    }

    /**
     * Create a UResource from an ID. This method will determine if
     * the id is an RPC or topic ID based on the range
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    fun from(id: Int): UResource {
        return if (id < MAX_RPC_ID) createForRpcRequest(id = id) else uResource { this.id = id }
    }

    /**
     * Build a UResource from a protobuf message. This method will determine if
     * the message is a RPC or topic message based on the message type
     * @param message The protobuf message.
     * @return Returns a UResource for an RPC request.
     */
    fun from(message: ProtocolMessageEnum): UResource {
        return uResource {
            message.descriptorForType.containingType.name?.let { name = it }
            message.valueDescriptor.name?.let { instance = it }
            id = message.number
        }
    }

}
