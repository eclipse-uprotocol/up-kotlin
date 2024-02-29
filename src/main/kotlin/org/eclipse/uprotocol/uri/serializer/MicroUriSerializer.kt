/*
 * Copyright (c) 2024 General Motors GTO LLC
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

import com.google.protobuf.ByteString
import org.eclipse.uprotocol.uri.factory.UResourceBuilder
import org.eclipse.uprotocol.uri.validator.UriValidator
import org.eclipse.uprotocol.v1.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * UUri Serializer that serializes a UUri to a byte[] (micro format) per
 * [...](https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc)
 */
class MicroUriSerializer private constructor() : UriSerializer<ByteArray> {
    /**
     * The type of address used for Micro URI.
     */
    private enum class AddressType(private val value: Int) {
        LOCAL(0), IPv4(1), IPv6(2), ID(3);

        fun getValue(): Byte {
            return value.toByte()
        }

        companion object {
            fun from(value: Int): Optional<AddressType> {
                return Arrays.stream(entries.toTypedArray()).filter { p -> p.getValue().toInt() == value }.findAny()
            }
        }
    }

    /**
     * Serialize a UUri into a byte[] following the Micro-URI specifications.
     * @param uri The [UUri] data object.
     * @return Returns a byte[] representing the serialized [UUri].
     */
    override fun serialize(uri: UUri?): ByteArray {
        if (uri == null || UriValidator.isEmpty(uri) || !UriValidator.isMicroForm(uri)) {
            return ByteArray(0)
        }
        val maybeUeId: Optional<Int> = Optional.ofNullable(uri.entity.id)
        val maybeUResourceId: Optional<Int> = Optional.ofNullable(uri.resource.id)
        val os = ByteArrayOutputStream()
        // UP_VERSION
        os.write(UP_VERSION.toInt())
        val type = if (uri.authority.hasIp()) {
            val length = uri.authority.getIp().size()
            if (length == 4) {
                AddressType.IPv4
            } else if (length == 16) {
                AddressType.IPv6
            } else {
                return ByteArray(0)
            }
        } else if (uri.authority.hasId()) {
            AddressType.ID
        } else {
            AddressType.LOCAL
        }

        os.write(type.getValue().toInt())

        // URESOURCE_ID
        os.write(maybeUResourceId.get() shr 8)
        os.write(maybeUResourceId.get())

        // UENTITY_ID
        os.write(maybeUeId.get() shr 8)
        os.write(maybeUeId.get())

        // UE_VERSION
        os.write((if (uri.entity.versionMajor == 0) 0.toByte() else uri.entity.versionMajor).toInt())

        // UNUSED
        os.write(0.toByte().toInt())


        // Populating the UAuthority
        if (type != AddressType.LOCAL) {

            // Write the ID length if the type is ID
            if (type == AddressType.ID) {
                os.write(uri.authority.id.size())
            }
            try {
                if (uri.authority.hasIp()) {
                    os.write(uri.authority.getIp().toByteArray())
                } else if (uri.authority.hasId()) {
                    os.write(uri.authority.getId().toByteArray())
                }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
        return os.toByteArray()
    }

    /**
     * Deserialize a byte[] into a [UUri] object.
     * @param uri A byte[] uProtocol micro URI.
     * @return Returns an [UUri] data object from the serialized format of a microUri.
     */
    override fun deserialize(uri: ByteArray?): UUri {
        if (uri == null || uri.size < LOCAL_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance()
        }

        // Need to be version 1
        if (uri[0].toInt() != 0x1) {
            return UUri.getDefaultInstance()
        }
        val uResourceId = uri[2].toInt() and 0xFF shl 8 or (uri[3].toInt() and 0xFF)
        val type: Optional<AddressType> = AddressType.from(
            uri[1].toInt()
        )

        // Validate Type is found
        if (type.isEmpty) {
            return UUri.getDefaultInstance()
        }

        // Validate that the microUri is the correct length for the type
        val addressType: AddressType = type.get()
        if (addressType == AddressType.LOCAL && uri.size != LOCAL_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance()
        } else if (addressType == AddressType.IPv4 && uri.size != IPV4_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance()
        } else if (addressType == AddressType.IPv6 && uri.size != IPV6_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance()
        }

        // UENTITY_ID
        val ueId = uri[4].toInt() and 0xFF shl 8 or (uri[5].toInt() and 0xFF)

        // UE_VERSION
        val uiVersion: Int = uri[6].toUByte().toInt()

        // Calculate uAuthority
        var uAuthority: UAuthority? = null
        when (addressType) {

            AddressType.IPv4, AddressType.IPv6 -> uAuthority = uAuthority {
                ip = ByteString.copyFrom(
                    uri, 8, if (addressType == AddressType.IPv4) 4 else 16
                )
            }

            AddressType.ID -> {
                val length: Int = uri[8].toUByte().toInt()

                uAuthority = uAuthority {
                    id = ByteString.copyFrom(
                        uri, 9, length
                    )
                }
            }

            else -> {}
        }

        return uUri {
            entity = uEntity {
                id = ueId
                versionMajor = uiVersion
            }
            resource = UResourceBuilder.fromId(uResourceId)

            if (uAuthority != null) {
                authority = uAuthority
            }
        }
    }

    companion object {
        const val LOCAL_MICRO_URI_LENGTH = 8 // local micro URI length
        const val IPV4_MICRO_URI_LENGTH = 12 // IPv4 micro URI length
        const val IPV6_MICRO_URI_LENGTH = 24 // IPv6 micro UriPart length
        const val UP_VERSION: Byte = 0x1 // UP version
        private val INSTANCE = MicroUriSerializer()
        fun instance(): MicroUriSerializer {
            return INSTANCE
        }
    }
}
