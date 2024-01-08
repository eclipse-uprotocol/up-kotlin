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

package org.eclipse.uprotocol.uri.builder

import org.eclipse.uprotocol.v1.UResource
import org.eclipse.uprotocol.v1.uResource
import java.util.*

interface UResourceBuilder {
    companion object {
        private const val MAX_RPC_ID = 1000

        /**
         * Builds a UResource for an RPC response.
         * @return Returns a UResource for an RPC response.
         */
        fun forRpcResponse(): UResource {
            return uResource {
                name = "rpc"
                instance = "response"
                id = 0
            }
        }

        /**
         * Builds a UResource for an RPC request.
         * @param method The method to be invoked.
         * @return Returns a UResource for an RPC request.
         */
        fun forRpcRequest(method: String): UResource {
            return forRpcRequest(method, null)
        }

        /**
         * Builds a UResource for an RPC request with an ID and method name
         * @param method The method to be invoked.
         * @param id The ID of the request.
         * @return Returns a UResource for an RPC request.
         */
        fun forRpcRequest(method: String?, idRes: Int?): UResource {
            return uResource {
                name = "rpc"
                if (method != null) instance = method
                if (idRes != null) id = idRes
            }
        }

        /**
         * Builds a UResource for an RPC request with an ID
         * @param id The ID of the request.
         * @return Returns a UResource for an RPC request.
         */
        fun forRpcRequest(id: Int): UResource {
            return forRpcRequest(null, id)
        }

        /**
         * Build a UResource from an ID. This method will determine if
         * the id is an RPC or topic ID based on the range
         * @param id The ID of the request.
         * @return Returns a UResource for an RPC request.
         */
        fun fromId(idRes: Int): UResource {
            Objects.requireNonNull(idRes, "id cannot be null")
            return if (idRes < MAX_RPC_ID) forRpcRequest(idRes) else uResource { id= idRes}
        }
    }
}
