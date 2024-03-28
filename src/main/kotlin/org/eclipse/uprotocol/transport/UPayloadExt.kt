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

import com.google.protobuf.Any
import com.google.protobuf.Internal
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import org.eclipse.uprotocol.v1.UPayload
import org.eclipse.uprotocol.v1.UPayloadFormat


/**
 * Unpack a uPayload into a google.protobuf.Message.
 *
 * @param payload the payload to unpack
 * @param clazz the class of the message to unpack
 * @return the unpacked message
 */
@Suppress("UNCHECKED_CAST")
fun <T : Message> unpack(payload: UPayload, clazz: Class<T>): T? {
    if (!payload.hasValue()) {
        return null
    }
    return try {
        when (payload.format) {
            UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY -> {
                Any.parseFrom(payload.value).unpack(clazz)
            }

            UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF -> {
                val defaultInstance = Internal.getDefaultInstance(clazz)
                defaultInstance.parserForType.parseFrom(payload.value) as T
            }

            else -> null
        }
    } catch (e: InvalidProtocolBufferException) {
        null
    }
}

/**
 * Inline function to unpack a uPayload into a google.protobuf.Message.
 *
 * @return the unpacked message
 */
inline fun <reified T : Message> UPayload.unpack(): T? {
    return unpack(this, T::class.java)
}

