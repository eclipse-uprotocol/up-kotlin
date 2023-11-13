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

package org.eclipse.uprotocol.transport.datamodel

import org.eclipse.uprotocol.v1.UPayloadFormat
import java.util.*

/**
 * The UPayload contains the clean Payload information along with its raw serialized structure of a byte[].
 */
class UPayload(data: ByteArray?, hint: UPayloadFormat?) {
    private val data: ByteArray
    private val hint // Hint regarding the bytes contained within the UPayload
            : UPayloadFormat


    init {
        this.data = Objects.requireNonNullElse(data, ByteArray(0))
        this.hint = Objects.requireNonNullElse(hint, UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED)
    }

    /**
     * The actual serialized or raw data, which can be deserialized or simply used as is.
     * @return Returns the actual serialized or raw data, which can be deserialized or simply used as is.
     */
    fun data(): ByteArray {
        return data
    }

    /**
     * The hint regarding the bytes contained within the UPayload.
     * @return Returns the hint regarding the bytes contained within the UPayload.
     */
    fun hint(): UPayloadFormat {
        return hint
    }

    val isEmpty: Boolean
        /**
         * @return Returns true if the data in the UPayload is empty.
         */
        get() = data.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val uPayload = other as UPayload
        return data.contentEquals(uPayload.data) && hint == uPayload.hint
    }

    override fun hashCode(): Int {
        return Objects.hash(data.contentHashCode(), hint)
    }

    override fun toString(): String {
        return "UPayload{" +
                "data=" + data().contentToString() +
                ", hint=" + hint + '}'
    }

    companion object {
        private val EMPTY = UPayload(ByteArray(0), UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED)

        /**
         * @return Returns an empty representation of UPayload.
         */
        fun empty(): UPayload {
            return EMPTY
        }
    }
}
