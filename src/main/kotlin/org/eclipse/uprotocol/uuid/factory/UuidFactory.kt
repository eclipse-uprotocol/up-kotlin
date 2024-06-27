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

package org.eclipse.uprotocol.uuid.factory

import com.github.f4b6a3.uuid.UuidCreator
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.uUID
import java.time.Instant
import java.util.*

/**
 * The UuidFactory class is an abstract class that provides a factory method to
 * create UUIDs based on [rfc9562](https://www.rfc-editor.org/rfc/rfc9562).
 * The UuidFactory class provides two implementations, UUIDv6 (used for older versions)
 * of the protocol, and UUIDv7.
 */
sealed class UuidFactory {
    /**
     * Create a UUID based on the given time, or the current time if not provided.
     *
     * @param instant the time
     * @return a UUID
     */
    abstract operator fun invoke(instant: Instant = Instant.now()): UUID
}

/**
 * The Uuidv6Factory class is an implementation of the UuidFactory class that
 * creates UUIDs based on the UUIDv6 version of the protocol.
 */
data object UUIDV6 : UuidFactory() {
    override operator fun invoke(instant: Instant): UUID {
        val uuidJava: java.util.UUID = UuidCreator.getTimeOrdered(
            instant, null, null
        )
        return uUID {
            msb = uuidJava.mostSignificantBits
            lsb = uuidJava.leastSignificantBits
        }
    }
}

/**
 * The Uuidv7Factory class is an implementation of the UuidFactory class that
 * creates UUIDs based on the UUIDv7 version of the protocol.
 */
data object UUIDV7 : UuidFactory() {
    override operator fun invoke(instant: Instant): UUID {
        val time: Long = instant.toEpochMilli()

        val randA = Random().nextInt() and 0xfff
        val randB = Random().nextLong() and 0x3fffffffffffffffL
        return uUID {
            msb  = (time shl 16) or (7L shl 12) or randA.toLong()
            lsb = randB or (1L shl 63)
        }
    }
}
