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
package org.eclipse.uprotocol.uuid.serializer

import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.uUID


/**
 * Deserialize from a specific serialization format to a [UUID].
 * @return Returns the [UUID] object
 */
fun String.deserializeAsUUID(): UUID {
    return try {
        require(this.isNotBlank())
        val uuidJava = java.util.UUID.fromString(this)
        uUID {
            msb = uuidJava.mostSignificantBits
            lsb = uuidJava.leastSignificantBits
        }
    } catch (e: IllegalArgumentException) {
        UUID.getDefaultInstance()
    }
}

/**
 * Serialize from a [UUID] to a specific serialization format.
 * @return Returns the [UUID] in the transport serialized format.
 */
fun UUID.serialize(): String {
    return java.util.UUID(msb, lsb).toString()
}

