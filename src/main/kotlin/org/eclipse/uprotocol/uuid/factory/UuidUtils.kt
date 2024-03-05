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

package org.eclipse.uprotocol.uuid.factory

import com.github.f4b6a3.uuid.enums.UuidVariant
import com.github.f4b6a3.uuid.util.UuidTime
import com.github.f4b6a3.uuid.util.UuidUtil
import org.eclipse.uprotocol.v1.UUID


/**
 * Fetch the UUID version.
 *
 * @return the UUID version from the UUID object or Optional.empty() if the uuid is null.
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
 * Verify if version is a formal UUIDv8 uProtocol ID.
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
 * Verify uuid is either v6 or v8
 *
 * @return true if is UUID version 6 or 8
 */
fun UUID.isUuid(): Boolean = isUProtocol() || isUuidv6()

/**
 * Return the number of milliseconds since unix epoch from a passed UUID.
 *
 * @return number of milliseconds since unix epoch or empty if uuid is null.
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
    VERSION_UPROTOCOL(8);

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
