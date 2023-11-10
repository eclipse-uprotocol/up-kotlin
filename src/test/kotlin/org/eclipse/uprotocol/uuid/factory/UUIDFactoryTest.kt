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
package org.eclipse.uprotocol.uuid.factory

import org.eclipse.uprotocol.v1.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.*

class UUIDFactoryTest {
    @Test
    @DisplayName("Test UUIDv8 Creation")
    fun test_uuidv8_creation() {
        val now: Instant = Instant.now()
        val uuid: UUID = UUIDFactory.Factories.UPROTOCOL.factory().create(now)
        val version: Optional<UUIDUtils.Version> = UUIDUtils.getVersion(uuid)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val bytes: Optional<ByteArray> = UUIDUtils.toBytes(uuid)
        val uuidString: Optional<String> = UUIDUtils.toString(uuid)
        assertNotNull(uuid)
        assertTrue(UUIDUtils.isUProtocol(uuid))
        assertTrue(UUIDUtils.isUuid(uuid))
        assertFalse(UUIDUtils.isUuidv6(uuid))
        assertTrue(version.isPresent())
        assertTrue(time.isPresent())
        assertEquals(time.get(), now.toEpochMilli())
        assertTrue(bytes.isPresent())
        assertTrue(uuidString.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(bytes.get())
        assertTrue(uuid1.isPresent())
        assertEquals(uuid, uuid1.get())
        val uuid2: Optional<UUID> = UUIDUtils.fromString(uuidString.get())
        assertTrue(uuid2.isPresent())
        assertEquals(uuid, uuid2.get())
    }

    @Test
    @DisplayName("Test UUIDv8 Creation with null Instant")
    fun test_uuidv8_creation_with_null_instant() {
        val uuid: UUID = UUIDFactory.Factories.UPROTOCOL.factory().create(null)
        val version: Optional<UUIDUtils.Version> = UUIDUtils.getVersion(uuid)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val bytes: Optional<ByteArray> = UUIDUtils.toBytes(uuid)
        val uuidString: Optional<String> = UUIDUtils.toString(uuid)
        assertNotNull(uuid)
        assertTrue(UUIDUtils.isUProtocol(uuid))
        assertTrue(UUIDUtils.isUuid(uuid))
        assertFalse(UUIDUtils.isUuidv6(uuid))
        assertTrue(version.isPresent())
        assertTrue(time.isPresent())
        assertTrue(bytes.isPresent())
        assertTrue(uuidString.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(bytes.get())
        assertTrue(uuid1.isPresent())
        assertEquals(uuid, uuid1.get())
        val uuid2: Optional<UUID> = UUIDUtils.fromString(uuidString.get())
        assertTrue(uuid2.isPresent())
        assertEquals(uuid, uuid2.get())
    }

    @Test
    @DisplayName("Test UUIDv8 overflow")
    fun test_uuidv8_overflow() {
        val uuidList: MutableList<UUID> = ArrayList()

        val MAX_COUNT = 4095

        // Build UUIDs above MAX_COUNT (4095) so we can test the limits
        val now: Instant = Instant.now()
        for (i in 0 until MAX_COUNT * 2) {
            uuidList.add(UUIDFactory.Factories.UPROTOCOL.factory().create(now))

            // Time should be the same as the 1st
            assertEquals(UUIDUtils.getTime(uuidList[0]), UUIDUtils.getTime(uuidList[i]))

            // Random should always remain the same be the same
            assertEquals(uuidList[0].getLsb(), uuidList[i].getLsb())
            if (i > MAX_COUNT) {
                assertEquals(uuidList[MAX_COUNT].getMsb(), uuidList[i].getMsb())
            }
        }
    }

    @Test
    @DisplayName("Test UUIDv6 creation with Instance")
    fun test_uuidv6_creation_with_instant() {
        val now: Instant = Instant.now()
        val uuid: UUID = UUIDFactory.Factories.UUIDV6.factory().create(now)
        val version: Optional<UUIDUtils.Version> = UUIDUtils.getVersion(uuid)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val bytes: Optional<ByteArray> = UUIDUtils.toBytes(uuid)
        val uuidString: Optional<String> = UUIDUtils.toString(uuid)
        assertNotNull(uuid)
        assertTrue(UUIDUtils.isUuidv6(uuid))
        assertTrue(UUIDUtils.isUuid(uuid))
        assertFalse(UUIDUtils.isUProtocol(uuid))
        assertTrue(version.isPresent())
        assertTrue(time.isPresent())
        assertEquals(time.get(), now.toEpochMilli())
        assertTrue(bytes.isPresent())
        assertTrue(uuidString.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(bytes.get())
        assertTrue(uuid1.isPresent())
        assertEquals(uuid, uuid1.get())
        val uuid2: Optional<UUID> = UUIDUtils.fromString(uuidString.get())
        assertTrue(uuid2.isPresent())
        assertEquals(uuid, uuid2.get())
    }

    @Test
    @DisplayName("Test UUIDv6 creation with null Instant")
    fun test_uuidv6_creation_with_null_instant() {
        val uuid: UUID = UUIDFactory.Factories.UUIDV6.factory().create(null)
        val version: Optional<UUIDUtils.Version> = UUIDUtils.getVersion(uuid)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val bytes: Optional<ByteArray> = UUIDUtils.toBytes(uuid)
        val uuidString: Optional<String> = UUIDUtils.toString(uuid)
        assertNotNull(uuid)
        assertTrue(UUIDUtils.isUuidv6(uuid))
        assertFalse(UUIDUtils.isUProtocol(uuid))
        assertTrue(UUIDUtils.isUuid(uuid))
        assertTrue(version.isPresent())
        assertTrue(time.isPresent())
        assertTrue(bytes.isPresent())
        assertTrue(uuidString.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(bytes.get())
        assertTrue(uuid1.isPresent())
        assertEquals(uuid, uuid1.get())
        val uuid2: Optional<UUID> = UUIDUtils.fromString(uuidString.get())
        assertTrue(uuid2.isPresent())
        assertEquals(uuid, uuid2.get())
    }

    @Test
    @DisplayName("Test UUIDUtils for Random UUID")
    fun test_uuidutils_for_random_uuid() {
        val uuid_java: java.util.UUID = java.util.UUID.randomUUID()
        val uuid: UUID = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
            .setLsb(uuid_java.getLeastSignificantBits()).build()
        val version: Optional<UUIDUtils.Version> = UUIDUtils.getVersion(uuid)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val bytes: Optional<ByteArray> = UUIDUtils.toBytes(uuid)
        val uuidString: Optional<String> = UUIDUtils.toString(uuid)
        assertNotNull(uuid)
        assertFalse(UUIDUtils.isUuidv6(uuid))
        assertFalse(UUIDUtils.isUProtocol(uuid))
        assertFalse(UUIDUtils.isUuid(uuid))
        assertTrue(version.isPresent())
        assertFalse(time.isPresent())
        assertTrue(bytes.isPresent())
        assertTrue(uuidString.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(bytes.get())
        assertTrue(uuid1.isPresent())
        assertEquals(uuid, uuid1.get())
        val uuid2: Optional<UUID> = UUIDUtils.fromString(uuidString.get())
        assertTrue(uuid2.isPresent())
        assertEquals(uuid, uuid2.get())
    }

    @Test
    @DisplayName("Test UUIDUtils for empty UUID")
    fun test_uuidutils_for_empty_uuid() {
        val uuid: UUID = UUID.newBuilder().setMsb(0L).setLsb(0L).build()
        val version: Optional<UUIDUtils.Version> = UUIDUtils.getVersion(uuid)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val bytes: Optional<ByteArray> = UUIDUtils.toBytes(uuid)
        val uuidString: Optional<String> = UUIDUtils.toString(uuid)
        assertNotNull(uuid)
        assertFalse(UUIDUtils.isUuidv6(uuid))
        assertFalse(UUIDUtils.isUProtocol(uuid))
        assertTrue(version.isPresent())
        assertEquals(version.get(), UUIDUtils.Version.VERSION_UNKNOWN)
        assertFalse(time.isPresent())
        assertTrue(bytes.isPresent())
        assertTrue(uuidString.isPresent())
        assertFalse(UUIDUtils.isUuidv6(null))
        assertFalse(UUIDUtils.isUProtocol(null))
        assertFalse(UUIDUtils.isUuid(null))
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(bytes.get())
        assertTrue(uuid1.isPresent())
        assertEquals(uuid, uuid1.get())
        val uuid2: Optional<UUID> = UUIDUtils.fromString(uuidString.get())
        assertTrue(uuid2.isPresent())
        assertEquals(uuid, uuid2.get())
    }

    @Test
    @DisplayName("Test UUIDUtils for a null UUID")
    fun test_uuidutils_for_null_uuid() {
        assertFalse(UUIDUtils.getVersion(null).isPresent())
        assertFalse(UUIDUtils.toBytes(null).isPresent())
        assertFalse(UUIDUtils.toString(null).isPresent())
        assertFalse(UUIDUtils.isUuidv6(null))
        assertFalse(UUIDUtils.isUProtocol(null))
        assertFalse(UUIDUtils.isUuid(null))
        assertFalse(UUIDUtils.getTime(null).isPresent())
    }

    @Test
    @DisplayName("Test UUIDUtils fromString an invalid built UUID")
    fun test_uuidutils_from_invalid_uuid() {
        val uuid: UUID = UUID.newBuilder().setMsb(9 shl 12).setLsb(0L).build() // Invalid UUID type
        assertFalse(UUIDUtils.getVersion(uuid).isPresent())
        assertFalse(UUIDUtils.getTime(uuid).isPresent())
        assertTrue(UUIDUtils.toBytes(uuid).isPresent())
        assertTrue(UUIDUtils.toString(uuid).isPresent())
        assertFalse(UUIDUtils.isUuidv6(uuid))
        assertFalse(UUIDUtils.isUProtocol(uuid))
        assertFalse(UUIDUtils.isUuid(uuid))
        assertFalse(UUIDUtils.getTime(uuid).isPresent())
    }

    @Test
    @DisplayName("Test UUIDUtils fromString with invalid string")
    fun test_uuidutils_fromstring_with_invalid_string() {
        val uuid: Optional<UUID> = UUIDUtils.fromString(null)
        assertFalse(uuid.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromString("")
        assertFalse(uuid1.isPresent())
    }

    @Test
    @DisplayName("Test UUIDUtils fromBytes with invalid bytes")
    fun test_uuidutils_frombytes_with_invalid_bytes() {
        val uuid: Optional<UUID> = UUIDUtils.fromBytes(null)
        assertFalse(uuid.isPresent())
        val uuid1: Optional<UUID> = UUIDUtils.fromBytes(ByteArray(0))
        assertFalse(uuid1.isPresent())
    }

    @Test
    @DisplayName("Test Create UProtocol UUID in the past")
    fun test_create_uprotocol_uuid_in_the_past() {
        val past: Instant = Instant.now().minusSeconds(10)
        val uuid: UUID = UUIDFactory.Factories.UPROTOCOL.factory().create(past)
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        assertTrue(UUIDUtils.isUProtocol(uuid))
        assertTrue(UUIDUtils.isUuid(uuid))
        assertTrue(time.isPresent())
        assertEquals(time.get(), past.toEpochMilli())
    }

    @Test
    @DisplayName("Test Create UProtocol UUID with different time values")
    @Throws(InterruptedException::class)
    fun test_create_uprotocol_uuid_with_different_time_values() {
        val uuid: UUID = UUIDFactory.Factories.UPROTOCOL.factory().create()
        Thread.sleep(10)
        val uuid1: UUID = UUIDFactory.Factories.UPROTOCOL.factory().create()
        val time: Optional<Long> = UUIDUtils.getTime(uuid)
        val time1: Optional<Long> = UUIDUtils.getTime(uuid1)
        assertTrue(UUIDUtils.isUProtocol(uuid))
        assertTrue(UUIDUtils.isUuid(uuid))
        assertTrue(UUIDUtils.isUProtocol(uuid1))
        assertTrue(UUIDUtils.isUuid(uuid1))
        assertTrue(time.isPresent())
        assertNotEquals(time.get(), time1.get())
    }

    @Test
    @DisplayName("Test Create both UUIDv6 and v8 to compare performance")
    @Throws(InterruptedException::class)
    fun test_create_both_uuidv6_and_v8_to_compare_performance() {
        val uuidv6List: MutableList<UUID> = ArrayList()
        val uuidv8List: MutableList<UUID> = ArrayList()
        val MAX_COUNT = 10000
        var start: Instant = Instant.now()
        for (i in 0 until MAX_COUNT) {
            uuidv8List.add(UUIDFactory.Factories.UPROTOCOL.factory().create())
        }
        val v8Diff: Duration = Duration.between(start, Instant.now())
        start = Instant.now()
        for (i in 0 until MAX_COUNT) {
            uuidv6List.add(UUIDFactory.Factories.UUIDV6.factory().create())
        }
        val v6Diff: Duration = Duration.between(start, Instant.now())
        System.out.println(
            (("UUIDv8:[" + v8Diff.toNanos() / MAX_COUNT).toString() + "ns]" + " UUIDv6:[" + v6Diff.toNanos() / MAX_COUNT).toString() + "ns]"
        )
    }
}
