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
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.uuid.factory

import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertNull

class UuidUtilsTest {

    private val testSource = uUri {
        entity = uEntity { name = "body.access" }
        resource = uResource {
            name = "door"
            instance = "front_left"
            message = "Door"
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetElapsedTime() {
        val testID = createId()
        Thread.sleep(DELAY_MS.toLong())
        //assertEquals(DELAY_MS, testID.getElapsedTime()?.toInt(), DELTA)
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
    fun testGetRemainingTimeAttributes() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
            ttl = TTL
        }
        assertEquals(TTL, attributes.getRemainingTime()?.toInt(), DELTA)
        Thread.sleep(DELAY_MS.toLong())
        assertEquals(TTL - DELAY_MS, attributes.getRemainingTime()?.toInt(), DELTA)
    }

    @Test
    fun testGetRemainingTimeAttributesNoTtl() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
        }
        assertNull(attributes.getRemainingTime())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGetRemainingTimeAttributesExpired() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
            ttl = DELAY_MS - DELTA
        }
        Thread.sleep(DELAY_MS.toLong())
        assertNull(attributes.getRemainingTime())
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
    @Throws(InterruptedException::class)
    fun testIsExpiredAttributes() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
            ttl = DELAY_MS - DELTA
        }
        assertFalse(attributes.isExpired())
        Thread.sleep(DELAY_MS.toLong())
        assertTrue(attributes.isExpired())
    }

    @Test
    fun testIsExpiredAttributesNoTtl() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
        }
        assertFalse(attributes.isExpired())
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