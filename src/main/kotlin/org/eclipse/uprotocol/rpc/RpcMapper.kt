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

package org.eclipse.uprotocol.rpc

import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UPayload
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.uStatus
import java.util.concurrent.CompletionException


/**
 * Map a response of Result&lt;UPayload&gt; from Link into a Result containing the declared expected return type of the RPC method or an exception.
 * @param expectedClazz The class name of the declared expected return type of the RPC method.
 * @return Returns Result containing the declared expected return type of the RPC method.
 * @param <T> The declared expected return type of the RPC method.
</T> */
fun <T : Message> Result<UPayload>.mapResponse(expectedClazz: Class<T>): Result<T> {
    return this.recoverCatching { exception ->
        throw CompletionException(exception.message, exception)
    }.mapCatching { payload ->
        val any = Any.parseFrom(payload.value)
        // Expected type
        if (any.`is`(expectedClazz)) {
            unpackPayload(any, expectedClazz)
        } else {
            throw RuntimeException("Unknown payload type [${any.typeUrl}]. Expected [${expectedClazz.name}]")
        }
    }
}

/**
 * Get the UStatus from Result&lt;Message&gt;.
 * @return Returns UStatus if Result contains UStatus or Exception, returns Null if Result contains other payload.
 * @param <T> The declared expected return type of the RPC method.
</T> */
fun <T : Message> Result<T>.getUStatusOrNull(): UStatus? {
    return this.fold({ payload ->
        if (payload is UStatus) {
            payload
        } else {
            null
        }
    }, { exception ->
        uStatus {
            code = UCode.UNKNOWN
            message = exception.message ?: ""
        }
    })
}

/**
 * Get the UStatus from Result&lt;UPayload&gt;.
 * @return Returns UStatus if Result contains UStatus or Exception.
 */
fun Result<UPayload>.mapUStatus(): UStatus {
    return this.mapResponse(UStatus::class.java).getUStatusOrNull()?: uStatus {
        code = UCode.UNKNOWN
        message = "unexpected error"
    }
}

/**
 * Unpack a payload of type [Any] into an object of type T, which is what was packing into the [Any] object.
 * @param payload an [Any] message containing a type of expectedClazz.
 * @param expectedClazz The class name of the object packed into the [Any]
 * @return Returns an object of type T and of the class name specified, that was packed into the [Any] object.
 * @param <T> The message type of the object packed into the [Any].
</T> */
private fun <T : Message> unpackPayload(payload: Any, expectedClazz: Class<T>): T {
    return try {
        payload.unpack(expectedClazz)
    } catch (e: InvalidProtocolBufferException) {
        throw RuntimeException("${e.message} [${UStatus::class.java.name}]", e)
    }
}
