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

package org.eclipse.uprotocol.cloudevent.serialize

import java.util.Base64

/**
 * Helper for serializing Base64 protobuf data.
 */
interface Base64ProtobufSerializer {
    companion object {
        /**
         * Deserialize a base64 protobuf payload into a Base64 String.
         * @param bytes byte[] data
         * @return Returns a String from the base64 protobuf payload.
         */
        fun deserialize(bytes: ByteArray?): String {
            return if (bytes == null) {
                ""
            } else Base64.getEncoder().encodeToString(bytes)
        }

        /**
         * Serialize a String into Base64 format.
         * @param stringToSerialize String to serialize.
         * @return Returns the Base64 formatted String as a byte[].
         */
        fun serialize(stringToSerialize: String?): ByteArray {
            return if (stringToSerialize == null) {
                ByteArray(0)
            } else Base64.getDecoder().decode(stringToSerialize.toByteArray())
        }
    }
}
