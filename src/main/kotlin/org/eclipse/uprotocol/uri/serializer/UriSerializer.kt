/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uri.serializer


import org.eclipse.uprotocol.uri.UUriConstant.MAJOR_VERSION_WILDCARD
import org.eclipse.uprotocol.uri.UUriConstant.WILDCARD_ID
import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.v1.UUri


/**
 * Support for serializing [UUri] objects into their String format.
 * @return Returns the String format of the supplied [UUri] that can be used as a sink or a source in a uProtocol publish communication.
 */
fun UUri.serialize(): String {
    if (isEmpty()) {
        return ""
    }

    val sb = StringBuilder()

    if (authorityName.isNotBlank()) {
        sb.append("//")
        sb.append(authorityName)
    }

    sb.append("/")
    sb.append(Integer.toHexString(ueId))
    sb.append("/")
    sb.append(Integer.toHexString(ueVersionMajor))
    sb.append("/")
    sb.append(Integer.toHexString(resourceId))
    return sb.toString().replace("/+$", "")
}
/**
 * Deserialize from the String to a [UUri].
 * @return Returns a [UUri] object from the serialized format from the wire.
 */
fun String.deserializeAsUUri(): UUri {
    if (isBlank()) {
        return UUri.getDefaultInstance()
    }
    val uuri: String = if (contains(":")) substring(indexOf(":") + 1) else
        replace('\\', '/')
    val isLocal: Boolean = !uuri.startsWith("//")
    val uriParts: List<String> = uuri.split("/").dropLastWhile { it.isEmpty() }
    val numberOfPartsInUri = uriParts.size
    if (numberOfPartsInUri == 0 || numberOfPartsInUri == 1) {
        return UUri.getDefaultInstance()
    }

    // TODO: optimize the logic
    val builder = UUri.newBuilder()
    try {
        if (isLocal) {
            builder.setUeId(Integer.parseUnsignedInt(uriParts[1], 16))
            if (numberOfPartsInUri > 2) {
                builder.setUeVersionMajor(Integer.parseUnsignedInt(uriParts[2], 16))

                if (numberOfPartsInUri > 3) {
                    builder.setResourceId(Integer.parseUnsignedInt(uriParts[3], 16))
                }
            }
        } else {
            // If authority is blank, it is an error
            if (uriParts[2].isBlank()) {
                return UUri.getDefaultInstance()
            }
            builder.setAuthorityName(uriParts[2])

            if (uriParts.size > 3) {
                builder.setUeId(Integer.parseUnsignedInt(uriParts[3], 16))
                if (numberOfPartsInUri > 4) {
                    builder.setUeVersionMajor(Integer.parseUnsignedInt(uriParts[4], 16))

                    if (numberOfPartsInUri > 5) {
                        builder.setResourceId(Integer.parseUnsignedInt(uriParts[5], 16))
                    }
                }
            }
        }
    } catch (e: NumberFormatException) {
        return UUri.getDefaultInstance()
    }


    // Ensure the major version is less than the wildcard
    if (builder.ueVersionMajor > MAJOR_VERSION_WILDCARD) {
        return UUri.getDefaultInstance()
    }


    // Ensure the resource id is less than the wildcard
    if (builder.resourceId > WILDCARD_ID) {
        return UUri.getDefaultInstance()
    }

    return builder.build()
}

