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

import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.math.abs
import kotlin.test.assertNull

class UuidUtilsTest {
    @Test
    @Throws(InterruptedException::class)
    fun testGetElapsedTime() {
        val testID = createId()
        Thread.sleep(DELAY_MS.toLong())
        assertEquals(DELAY_MS, testID.getElapsedTime()?.toInt(), DELTA)
    }

    private fun createId(): UUID {
        return UUIDV8()
    }

    @Test
    fun testGetElapsedTimeCreationTimeUnknown() {
        assertNull(UUID.getDefaultInstance().getElapsedTime())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetRemainingTime() {
        val id = createId()
        assertEquals(TTL, id.getRemainingTime(TTL)?.toInt(), DELTA)
        Thread.sleep(DELAY_MS.toLong())
        assertEquals(TTL - DELAY_MS, id.getRemainingTime(TTL)?.toInt(), DELTA)
    }

    @Test
    fun testGetRemainingTimeNoTtl() {
        val id = createId()
        assertNull(id.getRemainingTime(0))
        assertNull(id.getRemainingTime(-1))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetRemainingTimeExpired() {
        val id = createId()
        Thread.sleep(DELAY_MS.toLong())
        assertNull(id.getRemainingTime(DELAY_MS - DELTA))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testIsExpired() {
        val id = createId()
        assertFalse(id.isExpired(DELAY_MS - DELTA))
        Thread.sleep(DELAY_MS.toLong())
        assertTrue(id.isExpired(DELAY_MS - DELTA))
    }

    @Test
    fun testIsExpiredNoTtl() {
        val id = createId()
        assertFalse(id.isExpired(0))
        assertFalse(id.isExpired(-1))
    }

    @Test
    @DisplayName("Test getElapseTime() when UUID time is in the future")
    fun testGetElapsedTimePast() {
        val now = Instant.now().plusMillis(DELAY_MS.toLong())
        val id: UUID = UUIDV8(now)
        assertNull(id.getElapsedTime())
    }

    private fun assertEquals(expect: Int, actual: Int?, delta: Int?) {
        checkNotNull(actual)
        assertTrue(abs(expect - actual) <= (delta ?: 0))
    }

    companion object {
        private const val DELTA = 30
        private const val DELAY_MS = 100
        private const val TTL = 10000
    }
}