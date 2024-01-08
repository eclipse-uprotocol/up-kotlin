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
import org.eclipse.uprotocol.v1.uUID
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MicroUuidSerializer private constructor() : UuidSerializer<ByteArray?> {
    override fun deserialize(uuid: ByteArray?): UUID {
        if (uuid == null || uuid.size != 16) {
            return UUID.getDefaultInstance()
        }
        val byteBuffer = ByteBuffer.wrap(uuid)
        return uUID {
            msb = byteBuffer.getLong()
            lsb = byteBuffer.getLong()
        }
    }

    override fun serialize(uuid: UUID?): ByteArray {
        if (uuid == null) {
            return ByteArray(0)
        }
        val b = ByteArray(16)
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).putLong(uuid.msb).putLong(uuid.lsb).array()
    }

    companion object {
        private val INSTANCE = MicroUuidSerializer()
        fun instance(): MicroUuidSerializer {
            return INSTANCE
        }
    }
}
