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

package org.eclipse.uprotocol.uuid.factory

import com.github.f4b6a3.uuid.enums.UuidVariant
import com.github.f4b6a3.uuid.util.UuidTime
import com.github.f4b6a3.uuid.util.UuidUtil
import org.eclipse.uprotocol.v1.UUID

import java.util.Optional

/**
 * UUID Utils class that provides utility methods for uProtocol IDs
 */
interface UuidUtils {
    /**
     * UUID Version
     */
    enum class Version(val value: Int) {
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
             * @return The Version object or Optional.empty() if the value is not a valid version.
             */
            fun getVersion(value: Int): Optional<Version> {
                for (version in entries) {
                    if (version.value == value) {
                        return Optional.of(version)
                    }
                }
                return Optional.empty()
            }
        }
    }

    companion object {

        /**
         * Fetch the UUID version.
         *
         * @param uuid The UUID to fetch the version from.
         * @return the UUID version from the UUID object or Optional.empty() if the uuid is null.
         */
        fun getVersion(uuid: UUID?): Optional<Version> {
            // Version is bits masked by 0x000000000000F000 in MS long
            return if (uuid == null) Optional.empty() else Version.getVersion((uuid.msb shr 12 and 0x0f).toInt())
        }

        /**
         * Fetch the Variant from the passed UUID.
         *
         * @param uuid The UUID to fetch the variant from.
         * @return UUID variant or Empty if uuid is null.
         */
        fun getVariant(uuid: UUID?): Optional<Int> {
            return if (uuid == null) Optional.empty() else Optional.of(
                (uuid.lsb ushr (64 - (uuid.lsb ushr 62)).toInt() and (uuid.lsb shr 63)).toInt()
            )
        }

        /**
         * Verify if version is a formal UUIDv8 uProtocol ID.
         *
         * @return true if is a uProtocol UUID or false if uuid passed is null
         * or the UUID is not uProtocol format.
         */
        fun isUProtocol(uuid: UUID?): Boolean {
            val version: Optional<Version> = getVersion(uuid)
            return uuid != null && version.isPresent && version.get() === Version.VERSION_UPROTOCOL
        }

        /**
         * Verify if version is UUIDv6
         *
         * @return true if is UUID version 6 or false if uuid is null or not version 6
         */
        fun isUuidv6(uuid: UUID?): Boolean {
            val version: Optional<Version> = getVersion(uuid)
            val variant: Optional<Int> = getVariant(uuid)
            return uuid != null && version.isPresent && version.get() === Version.VERSION_TIME_ORDERED && variant.get() == UuidVariant.VARIANT_RFC_4122.value
        }

        /**
         * Verify uuid is either v6 or v8
         *
         * @return true if is UUID version 6 or 8
         */
        fun isUuid(uuid: UUID?): Boolean {
            return isUProtocol(uuid) || isUuidv6(uuid)
        }

        /**
         * Return the number of milliseconds since unix epoch from a passed UUID.
         *
         * @param uuid passed uuid to fetch the time.
         * @return number of milliseconds since unix epoch or empty if uuid is null.
         */
        fun getTime(uuid: UUID?): Optional<Long> {
            var time: Long? = null
            val version = getVersion(uuid)

            if (version.isEmpty) {
                    return Optional.empty()
                }

            when (version.get()) {
                Version.VERSION_UPROTOCOL -> {
                    time = uuid!!.msb ushr 16
                }
                Version.VERSION_TIME_ORDERED -> {
                    try {
                        val uuidJava = java.util.UUID(uuid!!.msb, uuid.lsb)
                        time = UuidTime.toUnixTimestamp(UuidUtil.getTimestamp(uuidJava)) / UuidTime.TICKS_PER_MILLI
                    } catch (e: IllegalArgumentException) {
                        return Optional.empty()
                    }
                }
                else -> {
                }
            }

            return Optional.ofNullable(time)
        }
    }
}