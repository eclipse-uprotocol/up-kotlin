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
        val uuid: UUID = UUIDV8(now)
        val version= uuid.getVersion()
        val time: Long? = uuid.getTime()
        val bytes = MicroUuidSerializer.INSTANCE.serialize(uuid)
        val uuidString = LongUuidSerializer.INSTANCE.serialize(uuid)

        assertNotNull(uuid)
        assertTrue(uuid.isUProtocol())
        assertTrue(uuid.isUuid())
        assertFalse(uuid.isUuidv6())
        assertNotNull(version)
        assertNotNull(time)
        assertEquals(now.toEpochMilli(), time)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())
        val uuid1: UUID = MicroUuidSerializer.INSTANCE.deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.INSTANCE.deserialize(uuidString)
        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertFalse(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UUIDv8 overflow")
    fun test_uuidv8_overflow() {
        val uuidList: MutableList<UUID> = ArrayList()

        val maxCount = 4095

        // Build UUIDs above MAX_COUNT (4095) so we can test the limits
        val now: Instant = Instant.now()
        for (i in 0 until maxCount * 2) {
            uuidList.add(UUIDV8(now))

            // Time should be the same as the 1st
            assertEquals(uuidList[0].getTime(), uuidList[i].getTime())

            // Random should always remain the same be the same
            assertEquals(uuidList[0].lsb, uuidList[i].lsb)
            if (i > maxCount) {
                assertEquals(uuidList[maxCount].msb, uuidList[i].msb)
            }
        }
    }

    @Test
    @DisplayName("Test UUIDv6 creation with Instance")
    fun test_uuidv6_creation_with_instant() {
        val now: Instant = Instant.now()
        val uuid: UUID = UUIDV6(now)
        val version = uuid.getVersion()
        val time: Long? = uuid.getTime()
        val bytes = MicroUuidSerializer.INSTANCE.serialize(uuid)
        val uuidString = LongUuidSerializer.INSTANCE.serialize(uuid)
        assertNotNull(uuid)
        assertTrue(uuid.isUuidv6())
        assertTrue(uuid.isUuid())
        assertFalse(uuid.isUProtocol())
        assertNotNull(version)
        assertNotNull(time)
        assertEquals(now.toEpochMilli(), time)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())
        val uuid1: UUID = MicroUuidSerializer.INSTANCE.deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.INSTANCE.deserialize(uuidString)
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
        val version = uuid.getVersion()
        val time: Long? = uuid.getTime()
        val bytes = MicroUuidSerializer.INSTANCE.serialize(uuid)
        val uuidString = LongUuidSerializer.INSTANCE.serialize(uuid)
        assertNotNull(uuid)
        assertFalse(uuid.isUuidv6())
        assertFalse(uuid.isUProtocol())
        assertFalse(uuid.isUuid())
        assertNotNull(version)
        assertNull(time)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())

        val uuid1: UUID = MicroUuidSerializer.INSTANCE.deserialize(bytes)
        val uuid2: UUID = LongUuidSerializer.INSTANCE.deserialize(uuidString)

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
        val version = uuid.getVersion()
        val time: Long? = uuid.getTime()
        val bytes = MicroUuidSerializer.INSTANCE.serialize(uuid)
        val uuidString = LongUuidSerializer.INSTANCE.serialize(uuid)
        assertNotNull(uuid)
        assertFalse(uuid.isUuidv6())
        assertFalse(uuid.isUProtocol())
        assertNotNull(version)
        assertEquals(UUIDVersion.VERSION_UNKNOWN, version)
        assertNull(time)
        assertTrue(bytes.isNotEmpty())
        assertFalse(uuidString.isBlank())

        val uuid1: UUID = MicroUuidSerializer.INSTANCE.deserialize(bytes)
        assertTrue(uuid1 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
        val uuid2: UUID = LongUuidSerializer.INSTANCE.deserialize(uuidString)
        assertTrue(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid2)
    }

    @Test
    @DisplayName("Test UuidUtils for a null UUID")
    fun test_uuidutils_for_null_uuid() {
        assertTrue(MicroUuidSerializer.INSTANCE.serialize(null).isEmpty())
        assertTrue(LongUuidSerializer.INSTANCE.serialize(null).isBlank())
    }

    @Test
    @DisplayName("Test UuidUtils fromString an invalid built UUID")
    fun test_uuidutils_from_invalid_uuid() {
        val uuid = uUID {
            msb = (9 shl 12).toLong()
            lsb = 0L
        } // Invalid UUID type
        assertEquals(UUIDVersion.VERSION_UNKNOWN, uuid.getVersion())
        assertNull(uuid.getTime())
        assertTrue(MicroUuidSerializer.INSTANCE.serialize(uuid).isNotEmpty())
        assertFalse(LongUuidSerializer.INSTANCE.serialize(uuid).isBlank())
        assertFalse(uuid.isUuidv6())
        assertFalse(uuid.isUProtocol())
        assertFalse(uuid.isUuid())
    }

    @Test
    @DisplayName("Test UuidUtils fromString with invalid string")
    fun test_uuidutils_fromstring_with_invalid_string() {
        val uuid: UUID = LongUuidSerializer.INSTANCE.deserialize(null)
        assertTrue(uuid == UUID.getDefaultInstance())
        val uuid1: UUID = LongUuidSerializer.INSTANCE.deserialize("")
        assertTrue(uuid1 == UUID.getDefaultInstance())
    }

    @Test
    @DisplayName("Test UuidUtils fromBytes with invalid bytes")
    fun test_uuidutils_frombytes_with_invalid_bytes() {
        val uuid: UUID = MicroUuidSerializer.INSTANCE.deserialize(null)
        assertTrue(uuid == UUID.getDefaultInstance())
        val uuid1: UUID = MicroUuidSerializer.INSTANCE.deserialize(ByteArray(0))
        assertTrue(uuid1 == UUID.getDefaultInstance())
    }

    @Test
    @DisplayName("Test Create UProtocol UUID in the past")
    fun test_create_uprotocol_uuid_in_the_past() {
        val past: Instant = Instant.now().minusSeconds(10)
        val uuid: UUID = UUIDV8(past)
        val time: Long? = uuid.getTime()
        assertTrue(uuid.isUProtocol())
        assertTrue(uuid.isUuid())
        assertNotNull(time)
        assertEquals(past.toEpochMilli(), time)
    }

    @Test
    @DisplayName("Test Create UProtocol UUID with different time values")
    @Throws(InterruptedException::class)
    fun test_create_uprotocol_uuid_with_different_time_values() {
        val uuid: UUID = UUIDV8()
        Thread.sleep(10)
        val uuid1: UUID = UUIDV8()
        val time: Long? = uuid.getTime()
        val time1: Long? = uuid1.getTime()
        assertTrue(uuid.isUProtocol())
        assertTrue(uuid.isUuid())
        assertTrue(uuid1.isUProtocol())
        assertTrue(uuid1.isUuid())
        assertNotNull(time)
        assertNotEquals(time, time1)
    }

    @Test
    @DisplayName("Test Create both UUIDv6 and v8 to compare performance")
    @Throws(InterruptedException::class)
    fun test_create_both_uuidv6_and_v8_to_compare_performance() {
        val uuidv6List: MutableList<UUID> = ArrayList()
        val uuidv8List: MutableList<UUID> = ArrayList()
        val maxCount = 10000
        var start: Instant = Instant.now()
        for (i in 0 until maxCount) {
            uuidv8List.add(UUIDV8())
        }
        val v8Diff: Duration = Duration.between(start, Instant.now())
        start = Instant.now()
        for (i in 0 until maxCount) {
            uuidv6List.add(UUIDV6())
        }
        val v6Diff: Duration = Duration.between(start, Instant.now())
        println((("UUIDv8:[" + v8Diff.toNanos() / maxCount) + "ns]" + " UUIDv6:[" + v6Diff.toNanos() / maxCount) + "ns]")
    }
}
