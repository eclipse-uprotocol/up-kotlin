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

import org.eclipse.uprotocol.uuid.serializer.deserializeAsUUID
import org.eclipse.uprotocol.uuid.serializer.serialize
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
    @DisplayName("Test UUIDV7 Creation")
    fun test_UUIDV7_creation() {
        val now: Instant = Instant.now()
        val uuid: UUID = UUIDV7(now)
        val version = uuid.getVersion()
        val time: Long? = uuid.getTime()
        val uuidString = uuid.serialize()

        assertNotNull(uuid)
        assertTrue(uuid.isUProtocol())
        assertTrue(uuid.isUuid())
        assertFalse(uuid.isUuidv6())
        assertNotNull(version)
        assertNotNull(time)
        assertEquals(now.toEpochMilli(), time)
        assertFalse(uuidString.isBlank())

        val uuid1: UUID = uuidString.deserializeAsUUID()
        assertFalse(uuid1 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid1)
    }

    @Test
    @DisplayName("Test UUIDv6 creation with Instance")
    fun test_uuidv6_creation_with_instant() {
        val now: Instant = Instant.now()
        val uuid: UUID = UUIDV6(now)
        val version = uuid.getVersion()
        val time: Long? = uuid.getTime()
        val uuidString = uuid.serialize()
        assertNotNull(uuid)
        assertTrue(uuid.isUuidv6())
        assertTrue(uuid.isUuid())
        assertFalse(uuid.isUProtocol())
        assertNotNull(version)
        assertNotNull(time)
        assertEquals(now.toEpochMilli(), time)

        assertFalse(uuidString.isBlank())
        val uuid2: UUID = uuidString.deserializeAsUUID()
        assertFalse(uuid2 == UUID.getDefaultInstance())
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
        val uuidString = uuid.serialize()
        assertNotNull(uuid)
        assertFalse(uuid.isUuidv6())
        assertFalse(uuid.isUProtocol())
        assertFalse(uuid.isUuid())
        assertNotNull(version)
        assertNull(time)

        assertFalse(uuidString.isBlank())

        val uuid2: UUID = uuidString.deserializeAsUUID()
        assertFalse(uuid2 == UUID.getDefaultInstance())
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
        val uuidString = uuid.serialize()
        assertNotNull(uuid)
        assertFalse(uuid.isUuidv6())
        assertFalse(uuid.isUProtocol())
        assertNotNull(version)
        assertEquals(UUIDVersion.VERSION_UNKNOWN, version)
        assertNull(time)
        assertFalse(uuidString.isBlank())

        val uuid2: UUID = uuidString.deserializeAsUUID()
        assertTrue(uuid2 == UUID.getDefaultInstance())
        assertEquals(uuid, uuid2)
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
        assertFalse(uuid.serialize().isBlank())
        assertFalse(uuid.isUuidv6())
        assertFalse(uuid.isUProtocol())
        assertFalse(uuid.isUuid())
    }

    @Test
    @DisplayName("Test UuidUtils fromString with invalid string")
    fun test_uuidutils_from_string_with_invalid_string() {
        val uuid1: UUID = "".deserializeAsUUID()
        assertTrue(uuid1 == UUID.getDefaultInstance())
    }

    @Test
    @DisplayName("Test Create UProtocol UUID in the past")
    fun test_create_uprotocol_uuid_in_the_past() {
        val past: Instant = Instant.now().minusSeconds(10)
        val uuid: UUID = UUIDV7(past)
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
        val uuid: UUID = UUIDV7()
        Thread.sleep(10)
        val uuid1: UUID = UUIDV7()
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
    @DisplayName("Test Create both UUIDv6 and v7 to compare performance")
    @Throws(InterruptedException::class)
    fun test_create_both_uuidv6_and_v7_to_compare_performance() {
        val uuidv6List: MutableList<UUID> = ArrayList()
        val UUIDV7List: MutableList<UUID> = ArrayList()
        val maxCount = 10000
        var start: Instant = Instant.now()
        for (i in 0 until maxCount) {
            UUIDV7List.add(UUIDV7())
        }
        val v8Diff: Duration = Duration.between(start, Instant.now())
        start = Instant.now()
        for (i in 0 until maxCount) {
            uuidv6List.add(UUIDV6())
        }
        val v6Diff: Duration = Duration.between(start, Instant.now())
        println((("UUIDV7:[" + v8Diff.toNanos() / maxCount) + "ns]" + " UUIDv6:[" + v6Diff.toNanos() / maxCount) + "ns]")
    }

    @Test
    @DisplayName("Test Create UUIDv7 with the same time to confirm the UUIDs are not the same")
    fun test_create_uuidv7_with_the_same_time_to_confirm_the_uuids_are_not_the_same() {
        val now = Instant.now()
        val uuid: UUID = UUIDV7(now)
        val uuid1: UUID = UUIDV7(now)
        assertNotEquals(uuid, uuid1)
        assertEquals(uuid1.getTime(), uuid.getTime())
    }
}
