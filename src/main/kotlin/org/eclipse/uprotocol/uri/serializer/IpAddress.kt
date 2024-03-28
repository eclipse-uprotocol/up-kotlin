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
 *
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.uri.serializer


object IpAddress {
    fun toBytes(ipAddress: String): ByteArray {
        return if (ipAddress.isEmpty()) {
            ByteArray(0)
        } else if (isValidIPv4Address(ipAddress)) {
            convertIPv4ToByteArray(ipAddress)
        } else if (isValidIPv6Address(ipAddress)) {
            convertIPv6ToByteArray(ipAddress)
        } else {
            ByteArray(0)
        }
    }

    fun isValid(ipAddress: String): Boolean {
        return ipAddress.isNotEmpty() && (isValidIPv4Address(ipAddress) || isValidIPv6Address(ipAddress))
    }

    private fun isValidIPv4Address(ipAddress: String): Boolean {
        val octetRange = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
        val ipv4Regex = """^$octetRange\.$octetRange\.$octetRange\.$octetRange$""".toRegex()
        return ipAddress.matches(ipv4Regex)
    }

    private fun convertIPv4ToByteArray(ipAddress: String): ByteArray {
        val octets = ipAddress.split(".")
        return ByteArray(4) { i -> octets[i].toInt().toByte() }
    }

    private fun isValidIPv6Address(ipAddress: String): Boolean {
        // Split the address into groups using the colon separator
        val groups = ipAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }
        // Check the number of groups
        if (groups.size > 8) {
            return false // Too many groups
        }

        var hasDoubleColon = false
        var emptyGroups = 0

        for (group in groups) {
            // Check for an empty group
            if (group.isEmpty()) {
                emptyGroups++
                // Double colon can only appear once
                if (emptyGroups > 1) {
                    return false
                }
                hasDoubleColon = true
            } else {
                // Check each character in the group
                for (element in group) {
                    // Check if the character is a valid hexadecimal digit
                    if (!isValidHexDigit(element)) {
                        return false
                    }
                }
            }
        }

        // Check if the address ends with a double colon
        if (ipAddress.endsWith(":")) {
            // We already had an empty group so crap out
            if (emptyGroups > 0) {
                return false
            }
            hasDoubleColon = true
        }

        // Check the final number of groups
        if (!hasDoubleColon && groups.size != 8) {
            return false // Not enough groups
        }

        return true
    }

    private fun isValidHexDigit(c: Char): Boolean {
        return (c in 'a'..'f') || (c in '0'..'9') || (c in 'A'..'F')
    }

    private fun convertIPv6ToByteArray(ipAddress: String): ByteArray {
        // Split the address into groups using the colon separator
        val groups = ipAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }
        // Initialize the byte array
        val ipAddressBytes = ByteArray(16)

        // Index to keep track of the current position in the byte array
        var index = 0

        for (group in groups) {
            // Check for an empty group
            if (group.isEmpty()) {
                // Calculate the number of empty groups needed
                val emptyGroups = 8 - (groups.size - 1)

                // Fill the empty groups with zeros
                repeat(emptyGroups * 2) {
                    ipAddressBytes[index++] = 0
                }
            } else {
                // Convert the group to a 16-bit integer
                val value = group.toInt(16)

                // Split the value into two bytes
                ipAddressBytes[index++] = ((value shr 8) and 0xFF).toByte()
                ipAddressBytes[index++] = (value and 0xFF).toByte()
            }
        }

        return ipAddressBytes
    }
}
