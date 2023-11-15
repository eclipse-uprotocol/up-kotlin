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
package org.eclipse.uprotocol.uuid.serializer

import org.eclipse.uprotocol.v1.UUID

/**
 * UUID Serializer interface used to serialize/deserialize UUIDs to/from a string
 */
class LongUuidSerializer private constructor() : UuidSerializer<String?> {
    override fun deserialize(uuid: String?): UUID {
        return if (uuid.isNullOrBlank()) {
            UUID.getDefaultInstance()
        } else try {
            val uuidJava = java.util.UUID.fromString(uuid)
            UUID.newBuilder().setMsb(uuidJava.mostSignificantBits)
                    .setLsb(uuidJava.leastSignificantBits).build()
        } catch (e: IllegalArgumentException) {
            UUID.getDefaultInstance()
        }
    }

    override fun serialize(uuid: UUID?): String {
        return if (uuid == null) "" else java.util.UUID(uuid.msb, uuid.lsb).toString()
    }

    companion object {
        private val INSTANCE = LongUuidSerializer()
        fun instance(): LongUuidSerializer {
            return INSTANCE
        }
    }
}
