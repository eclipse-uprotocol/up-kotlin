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

package org.eclipse.uprotocol.uri.serializer

import org.eclipse.uprotocol.uri.validator.UriValidator
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.copy
import org.eclipse.uprotocol.v1.uUri
import java.util.*

/**
 * UUris are used in transport layers and hence need to be serialized.
 * Each transport supports different serialization formats.
 * For more information, please refer to [...](https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc)
 * @param <T> The data structure that the UUri will be serialized into. For example String or byte[].
</T> */
interface UriSerializer<T> {
    /**
     * Deserialize from the format to a [UUri].
     * @param uri serialized UUri.
     * @return Returns a [UUri] object from the serialized format from the wire.
     */
    fun deserialize(uri: T?): UUri

    /**
     * Serialize from a [UUri] to a specific serialization format.
     * @param uri UUri object to be serialized to the format T.
     * @return Returns the [UUri] in the transport serialized format.
     */
    fun serialize(uri: UUri?): T

    /**
     * Build a fully resolved [UUri] from the serialized long format and the serializes micro format.
     * @param longUri [UUri] serialized as a Sting.
     * @param microUri [UUri] serialized as a byte[].
     * @return Returns a [UUri] object serialized from one of the forms.
     */
    fun buildResolved(longUri: String?, microUri: ByteArray?): Optional<UUri> {
        if (longUri.isNullOrEmpty() && (microUri == null || microUri.isEmpty())) {
            return Optional.of(UUri.getDefaultInstance())
        }

        val longUUri = LongUriSerializer.instance().deserialize(longUri)
        val microUUri = MicroUriSerializer.instance().deserialize(microUri)

        val uri = uUri {
            authority = microUUri.authority.copy {
                name = longUUri.authority.name
            }

            entity=microUUri.entity.copy {
                name = longUUri.entity.name
            }
            resource=longUUri.resource.copy {
                id = microUUri.resource.id
            }
        }

        return if (UriValidator.isResolved(uri)) Optional.of(uri) else Optional.empty()
    }
}