/*
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication

import com.google.protobuf.*
import com.google.protobuf.Internal.getDefaultInstance
import org.eclipse.uprotocol.v1.UMessage
import org.eclipse.uprotocol.v1.UPayloadFormat
import java.util.*

/**
 * Check if the payload is empty, returns true when what is passed is null or the data is empty.
 *
 * @return true if the payload is empty
 */
fun UPayload.isEmpty(): Boolean {
    return data.isEmpty && format == UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED
}


/**
 * Unpack a uMessage into [google.protobuf.Message].
 *
 * @return the unpacked message
 */
inline fun <reified T : Message?> UMessage.unpack(): T? {
    return UPayload.unpack(payload, attributes.payloadFormat)
}

/**
 * Unpack a uPayload into [Message]
 *
 * IMPORTANT NOTE: If [UPayloadFormat] is not [UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY],
 * there is no guarantee that the parsing to T is correct as we do not have the data schema.
 *
 * @return the unpacked message
 */
inline fun <reified T : Message> UPayload.unpack(): T? {
    return UPayload.unpack(data, format)
}

inline fun <reified T : Message> Result<UPayload>.mapToMessage(): Result<T> {
    return fold({ payload ->
        if (payload.data.isEmpty) {
            Result.success(getDefaultInstance(T::class.java))
        } else {
            payload.unpack<T>()?.let {
                Result.success(it)
            } ?: Result.failure(RuntimeException("Failed to unpack payload"))
        }
    }, { e ->
        Result.failure(e)
    })
}


