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

import com.github.f4b6a3.uuid.enums.UuidVariant
import com.github.f4b6a3.uuid.util.UuidTime
import com.github.f4b6a3.uuid.util.UuidUtil
import org.eclipse.uprotocol.v1.UUID


/**
 * Fetch the UUID version.
 *
 * @return the UUID version from the UUID object.
 */
fun UUID.getVersion(): UUIDVersion {
    // Version is bits masked by 0x000000000000F000 in MS long
    return UUIDVersion.getVersion((msb shr 12 and 0x0f).toInt())
}

/**
 * Fetch the Variant from the passed UUID.
 *
 * @return UUID variant.
 */
fun UUID.getVariant(): Int = (lsb ushr (64 - (lsb ushr 62)).toInt() and (lsb shr 63)).toInt()

/**
 * Verify if version is a formal UUIDv7 uProtocol ID.
 *
 * @return true if is a uProtocol UUID or false if the UUID is not uProtocol format.
 */
fun UUID.isUProtocol(): Boolean = getVersion() == UUIDVersion.VERSION_UPROTOCOL

/**
 * Verify if version is UUIDv6
 *
 * @return true if is UUID version 6 or false if uuid is not version 6
 */
fun UUID.isUuidv6(): Boolean =
    getVersion() == UUIDVersion.VERSION_TIME_ORDERED && getVariant() == UuidVariant.VARIANT_RFC_4122.value


/**
 * Verify uuid is either v6 or v7
 *
 * @return true if is UUID version 6 or 7
 */
fun UUID.isUuid(): Boolean = isUProtocol() || isUuidv6()

/**
 * Return the number of milliseconds since unix epoch from a passed UUID.
 *
 * @return number of milliseconds since unix epoch or NULL if uuid is null.
 */
fun UUID.getTime(): Long? {
    return when (getVersion()) {
        UUIDVersion.VERSION_UPROTOCOL -> {
            msb ushr 16
        }

        UUIDVersion.VERSION_TIME_ORDERED -> {
            try {
                val uuidJava = java.util.UUID(msb, lsb)
                UuidTime.toUnixTimestamp(UuidUtil.getTimestamp(uuidJava)) / UuidTime.TICKS_PER_MILLI
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        else -> {
            null
        }
    }
}

/**
 * Calculates the elapsed time since the creation of the specified UUID.
 *
 * @return The elapsed time in milliseconds or null if the creation time cannot be determined.
 */
fun UUID.getElapsedTime(): Long? {
    val creationTime: Long = getTime() ?: -1L
    if (creationTime < 0) {
        return null
    }
    return (System.currentTimeMillis() - creationTime).takeIf { it >= 0 }
}

/**
 * Calculates the remaining time until the expiration of the event identified by the given UUID.
 *
 * @param ttl The time-to-live (TTL) in milliseconds.
 * @return The remaining time in milliseconds until the event expires,
 * or null if TTL is non-positive, or the creation time cannot be determined.
 */
fun UUID.getRemainingTime(ttl: Int): Long? {
    if (ttl <= 0) {
        return null
    }
    return getElapsedTime()?.takeIf {
        ttl > it
    }?.let {
        ttl - it
    }
}

/**
 * Checks if the event identified by the given UUID has expired based on the specified time-to-live (TTL).
 *
 * @param ttl The time-to-live (TTL) in milliseconds for the event.
 * @return true if the event has expired, false otherwise. Returns false if TTL is non-positive or creation time
 * cannot be determined.
 */
fun UUID.isExpired(ttl: Int): Boolean {
    return ttl > 0 && getRemainingTime(ttl) == null
}

/**
 * UUID Version
 */
enum class UUIDVersion(val value: Int) {
    /**
     * An unknown version.
     */
    VERSION_UNKNOWN(0),

    /**
     * The randomly or pseudo-randomly generated version specified in RFC-4122.
     */
    VERSION_RANDOM_BASED(4),

    /**
     * The time-ordered version with gregorian epoch proposed by Peabody and Davis.
     */
    VERSION_TIME_ORDERED(6),

    /**
     * The custom or free-form version proposed by Peabody and Davis.
     */
    VERSION_UPROTOCOL(7);

    companion object {
        /**
         * Get the Version from the passed integer representation of the version.
         *
         * @param value The integer representation of the version.
         * @return The Version object or VERSION_UNKNOWN if the value is not a valid version.
         */

        fun getVersion(value: Int): UUIDVersion {
            return entries.find {
                it.value == value
            } ?: VERSION_UNKNOWN
        }
    }
}
