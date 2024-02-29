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
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UMessage
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
        if (!message.hasPayload()) {
            throw RuntimeException("Server returned a null payload. Expected [${T::class.java.name}]")
        }
        try {
            val any = Any.parseFrom(message.payload.value)
            if (any.`is`(T::class.java)) {
                any.unpack(T::class.java)
            } else {
                throw RuntimeException("Unknown payload type [${any.typeUrl}]. Expected [${T::class.java.name}]")
            }
        } catch (e: InvalidProtocolBufferException) {
            throw RuntimeException("${e.message} [${UStatus::class.java.name}]", e)
        }
    }
}

/**
 * Map a response of Flow&lt;Any&gt; from Link into a Flow containing a Result containing
 * the declared expected return type T.
 * @return Returns a Flow containing an Result containing the declared expected return type T, if T is UStatus
 * and has code not equals to OK, failure Result will be emitted.
 * @param <T> The declared expected return type of the RPC method.
 */
inline fun <reified T : Message> Flow<UMessage>.toResult(): Flow<Result<T>> {
    return toResponse<T>().map { response ->
        response.runCatching {
            if (this is UStatus && code != UCode.OK) {
                throw IllegalStateException("${message}, UStatus: $code")
            } else {
                this
            }
        }
    }.catch {
        emit(Result.failure(it))
    }
}
