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

package org.eclipse.uprotocol.rpc

import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.eclipse.uprotocol.v1.UMessage
import org.eclipse.uprotocol.v1.UPayload
import org.eclipse.uprotocol.v1.UStatus
import java.util.concurrent.CompletionException

/**
 * Inline function to map Flow&lt;UMessage&gt; from Link into a Flow containing the declared expected return type
 * of the RPC method or throw an exception.
 * @return Returns Flow containing the declared expected return type of the RPC method.
 * @param <T> The declared expected return type of the RPC method.
</T> */
inline fun <reified T : Message> Flow<UMessage>.toResponse(): Flow<T> {
    return catch { exception ->
        throw CompletionException(exception.message, exception)
    }.map { message ->
        if (!message.hasPayload()){
            throw RuntimeException("Server returned a null payload. Expected [${T::class.java.name}]")
        }
        val any = Any.parseFrom(message.payload.value)
        if (any.`is`(T::class.java)) {
            unpackPayload(any)
        } else {
            throw RuntimeException("Unknown payload type [${any.typeUrl}]. Expected [${T::class.java.name}]")
        }
    }
}

/**
 * Inline function to unpack a payload of type [Any] into an object of type T, which is what was
 * packing into the [Any] object.
 * @param payload an [Any] message containing a type of expectedClazz.
 * @return Returns an object of type T and of the class name specified, that was packed into the [Any] object.
 * @param <T> The message type of the object packed into the [Any].
 * </T> */
@PublishedApi
internal inline fun <reified T : Message> unpackPayload(payload: Any): T {
    return try {
        payload.unpack(T::class.java)
    } catch (e: InvalidProtocolBufferException) {
        throw RuntimeException("${e.message} [${UStatus::class.java.name}]", e)
    }
}
