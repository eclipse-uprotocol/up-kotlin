/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
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

import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.uuid.serializer.MicroUuidSerializer
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.uUID
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
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create(now)
        val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val bytes = MicroUuidSerializer.instance().serialize(uuid)
        val uuidString = LongUuidSerializer.instance().serialize(uuid)

        assertNotNull(uuid)
        assertTrue(UuidUtils.isUProtocol(uuid))
        assertTrue(UuidUtils.isUuid(uuid))
        assertFalse(UuidUtils.isUuidv6(uuid))
        assertTrue(version.isPresent)
        assertTrue(time.isPresent)
        assertEquals(time.get(), now.toEpochMilli())
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())
        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.instance().deserialize(uuidString)
        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertFalse(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UUIDv8 Creation with null Instant")
    fun test_uuidv8_creation_with_null_instant() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create(null)
        val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val bytes = MicroUuidSerializer.instance().serialize(uuid)
        val uuidString = LongUuidSerializer.instance().serialize(uuid)
        assertNotNull(uuid)
        assertTrue(UuidUtils.isUProtocol(uuid))
        assertTrue(UuidUtils.isUuid(uuid))
        assertFalse(UuidUtils.isUuidv6(uuid))
        assertTrue(version.isPresent)
        assertTrue(time.isPresent)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())
        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.instance().deserialize(uuidString)
        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertFalse(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UUIDv8 overflow")
    fun test_uuidv8_overflow() {
        val uuidList: MutableList<UUID> = ArrayList()

        val MAX_COUNT = 4095

        // Build UUIDs above MAX_COUNT (4095) so we can test the limits
        val now: Instant = Instant.now()
        for (i in 0 until MAX_COUNT * 2) {
            uuidList.add(UuidFactory.Factories.UPROTOCOL.factory().create(now))

            // Time should be the same as the 1st
            assertEquals(UuidUtils.getTime(uuidList[0]), UuidUtils.getTime(uuidList[i]))

            // Random should always remain the same be the same
            assertEquals(uuidList[0].lsb, uuidList[i].lsb)
            if (i > MAX_COUNT) {
                assertEquals(uuidList[MAX_COUNT].msb, uuidList[i].msb)
            }
        }
    }

    @Test
    @DisplayName("Test UUIDv6 creation with Instance")
    fun test_uuidv6_creation_with_instant() {
        val now: Instant = Instant.now()
        val uuid: UUID = UuidFactory.Factories.UUIDV6.factory().create(now)
        val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val bytes = MicroUuidSerializer.instance().serialize(uuid)
        val uuidString = LongUuidSerializer.instance().serialize(uuid)
        assertNotNull(uuid)
        assertTrue(UuidUtils.isUuidv6(uuid))
        assertTrue(UuidUtils.isUuid(uuid))
        assertFalse(UuidUtils.isUProtocol(uuid))
        assertTrue(version.isPresent)
        assertTrue(time.isPresent)
        assertEquals(time.get(), now.toEpochMilli())
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())
        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.instance().deserialize(uuidString)
        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertFalse(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UUIDv6 creation with null Instant")
    fun test_uuidv6_creation_with_null_instant() {
        val uuid: UUID = UuidFactory.Factories.UUIDV6.factory().create(null)
        val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val bytes = MicroUuidSerializer.instance().serialize(uuid)
        val uuidString = LongUuidSerializer.instance().serialize(uuid)
        assertNotNull(uuid)
        assertTrue(UuidUtils.isUuidv6(uuid))
        assertFalse(UuidUtils.isUProtocol(uuid))
        assertTrue(UuidUtils.isUuid(uuid))
        assertTrue(version.isPresent)
        assertTrue(time.isPresent)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())

        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.instance().deserialize(uuidString)

        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertFalse(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UuidUtils for Random UUID")
    fun test_uuidutils_for_random_uuid() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val uuid: UUID = uUID {
            msb = uuidJava.mostSignificantBits
            lsb = uuidJava.leastSignificantBits
        }
        val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val bytes = MicroUuidSerializer.instance().serialize(uuid)
        val uuidString = LongUuidSerializer.instance().serialize(uuid)
        assertNotNull(uuid)
        assertFalse(UuidUtils.isUuidv6(uuid))
        assertFalse(UuidUtils.isUProtocol(uuid))
        assertFalse(UuidUtils.isUuid(uuid))
        assertTrue(version.isPresent)
        assertFalse(time.isPresent)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())

        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.instance().deserialize(uuidString)

        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertFalse(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UuidUtils for empty UUID")
    fun test_uuidutils_for_empty_uuid() {
        val uuid: UUID = uUID {
            msb = 0L
            lsb = 0L
        }
        val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val bytes = MicroUuidSerializer.instance().serialize(uuid)
        val uuidString = LongUuidSerializer.instance().serialize(uuid)
        assertNotNull(uuid)
        assertFalse(UuidUtils.isUuidv6(uuid))
        assertFalse(UuidUtils.isUProtocol(uuid))
        assertTrue(version.isPresent)
        assertEquals(version.get(), UuidUtils.Version.VERSION_UNKNOWN)
        assertFalse(time.isPresent)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())
        assertFalse(UuidUtils.isUuidv6(null))
        assertFalse(UuidUtils.isUProtocol(null))
        assertFalse(UuidUtils.isUuid(null))

        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(bytes)
        assertTrue(uuid1 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        val uuid2: UUID = LongUuidSerializer.instance().deserialize(uuidString)
        assertTrue(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UuidUtils for a null UUID")
    fun test_uuidutils_for_null_uuid() {
        assertFalse(UuidUtils.getVersion(null).isPresent)
        assertTrue(MicroUuidSerializer.instance().serialize(null).isEmpty())
        assertTrue(LongUuidSerializer.instance().serialize(null).isBlank())
        assertFalse(UuidUtils.isUuidv6(null))
        assertFalse(UuidUtils.isUProtocol(null))
        assertFalse(UuidUtils.isUuid(null))
        assertFalse(UuidUtils.getTime(null).isPresent)
    }

    @Test
    @DisplayName("Test UuidUtils fromString an invalid built UUID")
    fun test_uuidutils_from_invalid_uuid() {
        val uuid = uUID {
            msb = (9 shl 12).toLong()
            lsb = 0L
        } // Invalid UUID type
        assertFalse(UuidUtils.getVersion(uuid).isPresent)
        assertFalse(UuidUtils.getTime(uuid).isPresent)
        assertTrue(MicroUuidSerializer.instance().serialize(uuid).isNotEmpty())
        assertFalse(LongUuidSerializer.instance().serialize(uuid).isBlank())
        assertFalse(UuidUtils.isUuidv6(uuid))
        assertFalse(UuidUtils.isUProtocol(uuid))
        assertFalse(UuidUtils.isUuid(uuid))
        assertFalse(UuidUtils.getTime(uuid).isPresent)
    }

    @Test
    @DisplayName("Test UuidUtils fromString with invalid string")
    fun test_uuidutils_fromstring_with_invalid_string() {
        val uuid: UUID = LongUuidSerializer.instance().deserialize(null)
        assertTrue(uuid == UUID.getDefaultInstance())
        val uuid1: UUID = LongUuidSerializer.instance().deserialize("")
        assertTrue(uuid1 == UUID.getDefaultInstance())
    }

    @Test
    @DisplayName("Test UuidUtils fromBytes with invalid bytes")
    fun test_uuidutils_frombytes_with_invalid_bytes() {
        val uuid: UUID = MicroUuidSerializer.instance().deserialize(null)
        assertTrue(uuid == UUID.getDefaultInstance())
        val uuid1: UUID = MicroUuidSerializer.instance().deserialize(ByteArray(0))
        assertTrue(uuid1 == UUID.getDefaultInstance())
    }

    @Test
    @DisplayName("Test Create UProtocol UUID in the past")
    fun test_create_uprotocol_uuid_in_the_past() {
        val past: Instant = Instant.now().minusSeconds(10)
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create(past)
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        assertTrue(UuidUtils.isUProtocol(uuid))
        assertTrue(UuidUtils.isUuid(uuid))
        assertTrue(time.isPresent)
        assertEquals(time.get(), past.toEpochMilli())
    }

    @Test
    @DisplayName("Test Create UProtocol UUID with different time values")
    @Throws(InterruptedException::class)
    fun test_create_uprotocol_uuid_with_different_time_values() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        Thread.sleep(10)
        val uuid1: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        val time1: Optional<Long> = UuidUtils.getTime(uuid1)
        assertTrue(UuidUtils.isUProtocol(uuid))
        assertTrue(UuidUtils.isUuid(uuid))
        assertTrue(UuidUtils.isUProtocol(uuid1))
        assertTrue(UuidUtils.isUuid(uuid1))
        assertTrue(time.isPresent)
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
            uuidv8List.add(UuidFactory.Factories.UPROTOCOL.factory().create())
        }
        val v8Diff: Duration = Duration.between(start, Instant.now())
        start = Instant.now()
        for (i in 0 until MAX_COUNT) {
            uuidv6List.add(UuidFactory.Factories.UUIDV6.factory().create())
        }
        val v6Diff: Duration = Duration.between(start, Instant.now())
        println((("UUIDv8:[" + v8Diff.toNanos() / MAX_COUNT) + "ns]" + " UUIDv6:[" + v6Diff.toNanos() / MAX_COUNT) + "ns]")
    }
}
