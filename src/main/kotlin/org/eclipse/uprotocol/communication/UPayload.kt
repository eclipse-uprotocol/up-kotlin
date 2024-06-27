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
import com.google.protobuf.Any
import org.eclipse.uprotocol.v1.UPayloadFormat


/**
 * Wrapper class that stores the payload as [UPayloadFormat].
 *
 */
data class UPayload(
    val data: ByteString = ByteString.EMPTY,
    val format: UPayloadFormat = UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED
) {
    companion object {

        /**
         *  Empty UPayload
         */
        val EMPTY: UPayload = UPayload()

        /**
         * Build a uPayload from [Message] by stuffing the message into an Any.
         *
         * @param message the message to pack
         * @return the UPayload
         */
        fun packToAny(message: Message): UPayload {
            return UPayload(
                Any.pack(message).toByteString(),
                UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY
            )
        }

        /**
         * Build a uPayload from [Message] using protobuf PayloadFormat.
         *
         * @param message the message to pack
         * @return the UPayload
         */
        fun pack(message: Message): UPayload {
            return UPayload(
                message.toByteString(),
                UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
            )
        }

        /**
         * Build a UPayload from specific data and passed format
         * @param data payload data.
         * @param format payload format.
         * @return the UPayload.
         */
        fun pack(data: ByteString, format: UPayloadFormat): UPayload {
            return UPayload(data, format)
        }

        /**
         * Unpack a uPayload into a [Message].
         * **IMPORTANT NOTE:** If [UPayloadFormat] is not [UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY],
         * there is no guarantee that the parsing to T is correct as we do not have the data schema.
         *
         * @param data The serialized UPayload data
         * @param format The serialization format of the payload
         * @return the unpacked message
         */
        inline fun <reified T : Message> unpack(data: ByteString, format: UPayloadFormat): T? {
            if (data.isEmpty) {
                return null
            }
            return try {
                when (format) {
                    UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY -> Any.parseFrom(
                        data
                    ).unpack(T::class.java)


                    UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF -> {
                        Internal.getDefaultInstance(T::class.java).parserForType.parseFrom(data) as T
                    }

                    else -> null
                }
            } catch (e: InvalidProtocolBufferException) {
                null
            }
        }
    }
}