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
import org.eclipse.uprotocol.uri.serializer.IpAddress.isValid
import org.eclipse.uprotocol.uri.serializer.IpAddress.toBytes
import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.v1.*
import java.net.InetAddress
import java.net.UnknownHostException


/**
 * UUri Serializer that serializes a UUri to a Short format string per
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
class ShortUriSerializer private constructor() : UriSerializer<String> {
    /**
     * Support for serializing {@link UUri} objects into their String format.
     * @param uri {@link UUri} object to be serialized to the String format.
     * @return Returns the String format of the supplied {@link UUri} that can be used
     * as a sink or a source in a uProtocol publish communication.
     */
    override fun serialize(uri: UUri): String {
        if (uri.isEmpty()) {
            return ""
        }

        val sb = StringBuilder()

        if (uri.hasAuthority()) {
            val authority: UAuthority = uri.authority
            if (authority.hasIp()) {
                try {
                    sb.append("/")
                    sb.append(InetAddress.getByAddress(authority.ip.toByteArray()))
                } catch (e: UnknownHostException) {
                    return ""
                }
            } else if (authority.hasId()) {
                sb.append("//")
                sb.append(authority.id.toStringUtf8())
            } else {
                return ""
            }
        }
        sb.append("/")

        sb.append(uri.entity.buildUriString())

        sb.append(uri.buildUriString())

        return sb.toString().replace("/+$", "")
    }

    /**
     * Deserialize a String into a UUri object.
     * @param uri A short format uProtocol URI.
     * @return Returns an UUri data object.
     */
    override fun deserialize(uri: String): UUri {
        if (uri.isBlank()) {
            return UUri.getDefaultInstance()
        }

        val uriString: String =
            if (uri.contains(":")) uri.substring(uri.indexOf(":") + 1) else uri.replace('\\', '/')

        val isLocal = !uriString.startsWith("//")

        val uriParts = uriString.split("/".toRegex()).dropLastWhile { it.isEmpty() }
        val numberOfPartsInUri = uriParts.size

        if (numberOfPartsInUri < 2) {
            return UUri.getDefaultInstance()
        }

        val uEId: String
        var ueVersion = ""

        var uResource: UResource? = null

        var uAuthority: UAuthority? = null

        if (isLocal) {
            uEId = uriParts[1]
            if (numberOfPartsInUri > 2) {
                ueVersion = uriParts[2]

                if (numberOfPartsInUri > 3) {
                    uResource = parseFromString(uriParts[3])
                }
                // Too many parts now
                if (numberOfPartsInUri > 4) {
                    return UUri.getDefaultInstance()
                }
            }
        } else {
            // If authority is blank, it is an error
            if (uriParts[2].isBlank()) {
                return UUri.getDefaultInstance()
            }

            // Try if it is an IP address, if not then it must be an ID
            uAuthority = uAuthority {
                if (isValid(uriParts[2])) {
                    ip = ByteString.copyFrom(toBytes(uriParts[2]))
                } else {
                    id = ByteString.copyFromUtf8(uriParts[2])
                }
            }

            if (uriParts.size > 3) {
                uEId = uriParts[3]
                if (numberOfPartsInUri > 4) {
                    ueVersion = uriParts[4]

                    if (numberOfPartsInUri > 5) {
                        uResource = parseFromString(uriParts[5])
                    }
                    // Way too many parts in the URI
                    if (numberOfPartsInUri > 6) {
                        return UUri.getDefaultInstance()
                    }
                }
            } else {
                return uUri {
                    authority = uAuthority
                }
            }
        }

        var useVersionInt: Int? = null
        var ueIdInt: Int? = null
        try {
            if (ueVersion.isNotBlank()) {
                useVersionInt = ueVersion.toInt()
            }

            if (uEId.isNotBlank()) {
                ueIdInt = uEId.toInt()
            }
        } catch (ignored: java.lang.NumberFormatException) {
            return UUri.getDefaultInstance()
        }

        return uUri {
            entity = uEntity {

                ueIdInt?.let { id = it }
                useVersionInt?.let { versionMajor = it }

            }
            uAuthority?.let { authority = it }
            uResource?.let { resource = it }
        }

    }

    companion object {
        val INSTANCE = ShortUriSerializer()

        /**
         * Static factory method for creating a UResource using a string value
         * @param resourceString String that contains the UResource id.
         * @return Returns a UResource object.
         */
        private fun parseFromString(resourceString: String): UResource {
            return try {
                uResource { from(resourceString.toInt()) }
            } catch (ignored: NumberFormatException) {
                UResource.getDefaultInstance()
            }
        }

        private fun UUri.buildUriString(): String {
            return if (hasResource()) {
                "/${resource.id}"
            } else {
                ""
            }
        }

        /**
         * Create the service part of the uProtocol URI from an software entity object.
         */
        private fun UEntity.buildUriString(): String {
            val versionStr = if (versionMajor > 0) {
                versionMajor
            } else {
                ""
            }
            return "$id/$versionStr"
        }
    }
}
