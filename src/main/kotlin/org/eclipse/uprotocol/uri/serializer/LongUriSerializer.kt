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
 */

package org.eclipse.uprotocol.uri.serializer

import java.util.Objects
import java.util.Optional
import org.eclipse.uprotocol.uri.validator.UriValidator
import org.eclipse.uprotocol.v1.UAuthority
import org.eclipse.uprotocol.v1.UEntity
import org.eclipse.uprotocol.v1.UResource
import org.eclipse.uprotocol.v1.UUri

/**
 * UUri Serializer that serializes a UUri to a long format string per
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
class LongUriSerializer private constructor() : UriSerializer<String?> {
    /**
     * Support for serializing [UUri] objects into their String format.
     * @param Uri [UUri] object to be serialized to the String format.
     * @return Returns the String format of the supplied [UUri] that can be used as a sink or a source in a uProtocol publish communication.
     */
    override fun serialize(uri: UUri?): String {
        if (uri == null || UriValidator.isEmpty(uri)) {
            return ""
        }
        val sb = StringBuilder()
        if (uri.hasAuthority()) {
            sb.append(buildAuthorityPartOfUri(uri.authority))
        }
        sb.append("/")
        sb.append(buildSoftwareEntityPartOfUri(uri.entity))
        sb.append(buildResourcePartOfUri(uri))
        return sb.toString().replace("/+$".toRegex(), "")
    }

    /**
     * Deserialize a String into a UUri object.
     * @param uri A long format uProtocol URI.
     * @return Returns an UUri data object.
     */

    override fun deserialize(uri: String?): UUri {
        if (uri.isNullOrBlank()) {
            return UUri.getDefaultInstance()
        }
        val uuri: String =
            if (uri.contains(":")) uri.substring(uri.indexOf(":") + 1) else uri
                .replace('\\', '/')
        val isLocal: Boolean = !uuri.startsWith("//")
        val uriParts: List<String> = removeEmpty(uuri.split("/".toRegex()))
        val numberOfPartsInUri = uriParts.size
        if (numberOfPartsInUri == 0 || numberOfPartsInUri == 1) {
            return UUri.getDefaultInstance()
        }
        val useName: String
        var useVersion = ""
        var uResource: UResource? = null
        var uAuthority: UAuthority? = null
        if (isLocal) {
            useName = uriParts[1]
            if (numberOfPartsInUri > 2) {
                useVersion = uriParts[2]
                if (numberOfPartsInUri > 3) {
                    uResource = parseFromString(
                        uriParts[3]
                    )
                }
            }
        } else {
            // If authority is blank, it is an error
            if (uriParts[2].isBlank()) {
                return UUri.getDefaultInstance()
            }
            uAuthority = UAuthority.newBuilder().setName(uriParts[2]).build()
            if (uriParts.size > 3) {
                useName = uriParts[3]
                if (numberOfPartsInUri > 4) {
                    useVersion = uriParts[4]
                    if (numberOfPartsInUri > 5) {
                        uResource = parseFromString(
                            uriParts[5]
                        )
                    }
                }
            } else {
                return UUri.newBuilder()
                    .setAuthority(uAuthority)
                    .build()
            }
        }
        var useVersionInt: Int? = null
        try {
            if (useVersion.isNotBlank()) {
                useVersionInt = Integer.valueOf(useVersion)
            }
        } catch (ignored: NumberFormatException) {
            return UUri.getDefaultInstance()
        }
        val uEntityBuilder: UEntity.Builder = UEntity.newBuilder().setName(useName)
        if (useVersionInt != null) {
            uEntityBuilder.setVersionMajor(useVersionInt)
        }
        val uriBuilder: UUri.Builder = UUri.newBuilder().setEntity(uEntityBuilder)
        if (uAuthority != null) {
            uriBuilder.setAuthority(uAuthority)
        }
        if (uResource != null) {
            uriBuilder.setResource(uResource)
        }
        return uriBuilder.build()
    }

    companion object {
        private val INSTANCE = LongUriSerializer()
        fun instance(): LongUriSerializer {
            return INSTANCE
        }

        private fun buildResourcePartOfUri(uri: UUri): String {
            if (!uri.hasResource()) {
                return ""
            }
            val uResource: UResource = uri.resource
            val sb = StringBuilder("/")
            sb.append(uResource.name)
            if (uResource.hasInstance()) {
                sb.append(".").append(uResource.instance)
            }
            if (uResource.hasMessage()) {
                sb.append("#").append(uResource.message)
            }
            return sb.toString()
        }

        /**
         * Create the service part of the uProtocol URI from an  software entity object.
         * @param use  Software Entity representing a service or an application.
         */
        private fun buildSoftwareEntityPartOfUri(use: UEntity): String {
            val sb = StringBuilder(use.name.trim())
            sb.append("/")
            if (use.versionMajor > 0) {
                sb.append(use.versionMajor)
            }
            return sb.toString()
        }

        /**
         * Create the authority part of the uProtocol URI from an  authority object.
         * @param uAuthority represents the deployment location of a specific  Software Entity.
         * @return Returns the String representation of the  Authority in the uProtocol URI.
         */
        private fun buildAuthorityPartOfUri(uAuthority: UAuthority): String {
            val partialURI = StringBuilder("//")
            val maybeName: Optional<String> = Optional.ofNullable(uAuthority.name)
            if (maybeName.isPresent) {
                partialURI.append(maybeName.get())
            }
            return partialURI.toString()
        }

        /**
         * Static factory method for creating a UResource using a string that contains
         * name + instance + message.
         * @param resourceString String that contains the UResource information.
         * @return Returns a UResource object.
         */
        private fun parseFromString(resourceString: String): UResource {
            Objects.requireNonNull(resourceString, " Resource must have a command name.")
            val parts: List<String> = removeEmpty(resourceString.split("#"))
            val nameAndInstance = parts[0]
            val nameAndInstanceParts: List<String> = removeEmpty(nameAndInstance.split("."))
            val resourceName = nameAndInstanceParts[0]
            val resourceInstance = if (nameAndInstanceParts.size > 1) nameAndInstanceParts[1] else null
            val resourceMessage = if (parts.size > 1) parts[1] else null
            val uResourceBuilder: UResource.Builder = UResource.newBuilder().setName(resourceName)
            if (resourceInstance != null) {
                uResourceBuilder.setInstance(resourceInstance)
            }
            if (resourceMessage != null) {
                uResourceBuilder.setMessage(resourceMessage)
            }
            return uResourceBuilder.build()
        }

        fun removeEmpty(parts: List<String>): List<String> {
            return if (parts.all { it.isEmpty() }) {
                emptyList()
            } else {
                parts
            }
        }
    }
}
