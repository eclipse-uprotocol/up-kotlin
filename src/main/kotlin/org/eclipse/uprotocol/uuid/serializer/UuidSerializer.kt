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
 * UUID Serializer interface used to serialize/deserialize UUIDs to/from either Long (string) or micro (bytes) form
 * @param <T> The data structure that the UUID will be serialized into. For example String or byte[].
</T> */
interface UuidSerializer<T> {
    /**
     * Deserialize from the format to a [UUID].
     * @param uuid serialized UUID.
     * @return Returns a [UUID] object from the serialized format from the wire.
     */
    fun deserialize(uuid: T): UUID

    /**
     * Serialize from a [UUID] to a specific serialization format.
     * @param uuid UUID object to be serialized to the format T.
     * @return Returns the [UUID] in the transport serialized format.
     */
    fun serialize(uuid: UUID?): T
}
