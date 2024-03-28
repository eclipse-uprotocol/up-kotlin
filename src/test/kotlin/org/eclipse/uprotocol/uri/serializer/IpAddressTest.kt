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
 */

package org.eclipse.uprotocol.uri.serializer

import org.eclipse.uprotocol.uri.serializer.IpAddress.isValid
import org.eclipse.uprotocol.uri.serializer.IpAddress.toBytes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.InetAddress

class IpAddressTest {
    @Test
    @DisplayName("Test toBytes with empty ipAddress")
    fun testToBytesWithEmptyIpAddress() {
        val bytes = toBytes("")
        assertEquals(0, bytes.size)
    }

    @Test
    @DisplayName("Test toBytes with invalid ipAddress")
    fun testToBytesWithInvalidIpAddress() {
        val bytes = toBytes("invalid")
        assertEquals(0, bytes.size)
    }

    @Test
    @DisplayName("Test toBytes with valid IPv4 address")
    fun testToBytesWithValidIPv4Address() {
        val bytes = toBytes("192.168.1.100")
        assertEquals(4, bytes.size)
        assertEquals(192, java.lang.Byte.toUnsignedInt(bytes[0]))
        assertEquals(168, java.lang.Byte.toUnsignedInt(bytes[1]))
        assertEquals(1, java.lang.Byte.toUnsignedInt(bytes[2]))
        assertEquals(100, java.lang.Byte.toUnsignedInt(bytes[3]))
    }

    @Test
    @DisplayName("Test toBytes with valid IPv6 address")
    fun testToBytesWithValidIPv6Address() {
        val bytes = toBytes("2001:db8:85a3:0:0:8a2e:370:7334")

        assertEquals(InetAddress.getByAddress(bytes).hostAddress, "2001:db8:85a3:0:0:8a2e:370:7334")
    }

    @Test
    @DisplayName("Test isValid with empty ipAddress")
    fun testIsValidWithEmptyIpAddress() {
        assertFalse(isValid(""))
    }

    @Test
    @DisplayName("Test isValid with invalid ipAddress")
    fun testIsValidWithInvalidIpAddress() {
        assertFalse(isValid("invalid"))
    }

    @Test
    @DisplayName("Test isValid with valid IPv4 address")
    fun testIsValidWithValidIPv4Address() {
        assertTrue(isValid("192.168.1.100"))
    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address")
    fun testIsValidWithValidIPv6Address() {
        assertTrue(isValid("2001:db8:85a3:0:0:8a2e:370:7334"))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 address")
    fun testIsValidWithInvalidIPv4Address() {
        val bytes = toBytes("192.168.1.2586")
        assertEquals(0, bytes.size)
        assertFalse(isValid("192.168.1.2586"))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 passing large number")
    fun testIsValidWithInvalidIPv4PassingLargeNumber() {
        val ipString = "2875687346587326457836485623874658723645782364875623847562378465.1.1.abc"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)

        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 passing negative value")
    fun testIsValidWithInvalidIPv4PassingNegative() {
        val ipString = "-1.1.1.abc"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 passing charaters")
    fun testIsValidWithInvalidIPv4PassingCharacters() {
        val ipString = "1.1.1.abc"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address")
    fun testIsValidWithInvalidIPv6Address() {
        val ipString = "ZX1:db8::"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address passing weird values")
    fun testIsValidWithInvalidIPv6AddressPassingWeirdValues() {
        val ipString = "-1:ZX1:db8::"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has way too many groups")
    fun testIsValidWithInvalidIPv6AddressThatHasWayTooManyGroups() {
        val ipString = "2001:db8:85a3:0:0:8a2e:370:7334:1234"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address that has 8 groups")
    fun testIsValidWithValidIPv6AddressThatHas8Groups() {
        val ipString = "2001:db8:85a3:0:0:8a2e:370:7334"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address with too many empty groups")
    fun testIsValidWithInvalidIPv6AddressWithTooManyEmptyGroups() {
        val ipString = "2001::85a3::8a2e::7334"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address with one empty group")
    fun testIsValidWithValidIPv6AddressWithOneEmptyGroup() {
        val ipString = "2001:db8:85a3::8a2e:370:7334"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that ends with a colon")
    fun testIsValidWithInvalidIPv6AddressThatEndsWithAColon() {
        val ipString = "2001:db8:85a3::8a2e:370:7334:"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has doesn't have double colon and not enough groups")
    fun testIsValidWithInvalidIPv6AddressThatHasDoesntHaveDoubleColonAndNotEnoughGroups() {
        val ipString = "2001:db8:85a3:0:0:8a2e:370"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address that ends with double colons")
    fun testIsValidWithValidIPv6AddressThatEndsWithDoubleColons() {
        val ipString = "2001:db8:85a3:8a2e::"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with all number values")
    fun testIsValidWithAllNumberValues() {
        val ipString = "123:456:7890::"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with valid lowercase hexidecimal letters")
    fun testIsValidWithValidLowercaseHexidecimalLetters() {
        val ipString = "abcd:ef12:3456::"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with valid uppercase hexidecimal letters")
    fun testIsValidWithValidUppercaseHexidecimalLetters() {
        val ipString = "ABCD:EF12:3456::"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid uppercase hexidecimal letters")
    fun testIsValidWithInvalidUppercaseHexidecimalLetters() {
        val ipString = "ABCD:EFG2:3456::"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid hexidecimal letters")
    fun testIsValidWithInvalidLowercaseHexidecimalLetters() {
        val ipString = "-C=[]:E{12g:3456"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid digit")
    fun testIsValidWithInvalidDigit1() {
        val ipString = "aC=[]:E{12g:3456"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid digit")
    fun testIsValidWithInvalidDigit2() {
        val ipString = "aCd[:E{12g:3456"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid digit")
    fun testIsValidWithInvalidDigit3() {
        val ipString = "aCd:E{2g:3456"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has double colon and 8 groups")
    fun testIsValidWithInvalidIPv6AddressThatHasDoubleColonAnd8Groups() {
        val ipString = "dead:beef:85a3::0:0:8a2e:370"
        val bytes = toBytes(ipString)
        assertEquals(16, bytes.size)
        assertTrue(isValid(ipString))
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has only has 7 groups")
    fun testIsValidWithInvalidIPv6AddressThatHasOnlyHas7Groups() {
        val ipString = "dead:beef:85a3:0:0:8a2e:370"
        val bytes = toBytes(ipString)
        assertEquals(0, bytes.size)
        assertFalse(isValid(ipString))
    }
}