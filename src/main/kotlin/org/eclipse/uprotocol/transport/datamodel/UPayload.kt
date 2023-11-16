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
data class UPayload(val data: ByteArray = ByteArray(0), val hint: UPayloadFormat = UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED) {

    val isEmpty: Boolean
        /**
         * @return Returns true if the data in the UPayload is empty.
         */
        get() = data.isEmpty()


    companion object {
        private val EMPTY = UPayload(ByteArray(0), UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED)

        /**
         * @return Returns an empty representation of UPayload.
         */
        fun empty(): UPayload {
            return EMPTY
        }
    }

    override fun toString(): String {
        return "UPayload{" + "data=" + data.contentToString() + ", hint=" + hint + '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UPayload

        if (!data.contentEquals(other.data)) return false
        if (hint != other.hint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + hint.hashCode()
        return result
    }
}
