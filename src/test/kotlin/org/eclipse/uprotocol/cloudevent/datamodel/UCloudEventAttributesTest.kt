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
package org.eclipse.uprotocol.cloudevent.datamodel

import nl.jqno.equalsverifier.EqualsVerifier
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes.Companion.uCloudEventAttributes
import org.eclipse.uprotocol.v1.UPriority
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class UCloudEventAttributesTest {
    @Test
    @DisplayName("Make sure the equals and hash code works")
    fun testHashCodeEquals() {
        EqualsVerifier.forClass(UCloudEventAttributes::class.java).usingGetClass().verify()
    }

    @Test
    @DisplayName("Make sure the default toString works")
    fun testToString() {
        val uCloudEventAttributes = uCloudEventAttributes {
            hash = "somehash"
            priority = UPriority.UPRIORITY_CS1
            ttl = 3
            token = "someOAuthToken"
            traceparent = "someTraceparent"
        }
        val expected = "UCloudEventAttributes(hash=somehash, priority=UPRIORITY_CS1, ttl=3, token=someOAuthToken, traceparent=someTraceparent)"
        assertEquals(expected, uCloudEventAttributes.toString())
    }

    @Test
    @DisplayName("Test creating a valid attributes object")
    fun test_create_valid() {
        val uCloudEventAttributes = uCloudEventAttributes {
            hash = "somehash"
            priority = UPriority.UPRIORITY_CS6
            ttl = 3
            token = "someOAuthToken"
            traceparent = "someTraceparent"
        }
        checkNotNull(uCloudEventAttributes.hash)
        assertEquals("somehash", uCloudEventAttributes.hash)
        checkNotNull(uCloudEventAttributes.priority)
        assertEquals(UPriority.UPRIORITY_CS6, uCloudEventAttributes.priority)
        checkNotNull(uCloudEventAttributes.ttl)
        assertEquals(3, uCloudEventAttributes.ttl)
        checkNotNull(uCloudEventAttributes.token)
        assertEquals("someOAuthToken", uCloudEventAttributes.token)
        checkNotNull(uCloudEventAttributes.traceparent)
        assertEquals("someTraceparent", uCloudEventAttributes.traceparent)
    }

    @Test
    @DisplayName("Test the isEmpty function")
    fun test_Isempty_function() {
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributes.EMPTY
        assertTrue(uCloudEventAttributes.isEmpty)
        assertTrue(uCloudEventAttributes.hash.isNullOrBlank())
        assertNull(uCloudEventAttributes.priority)
        assertNull(uCloudEventAttributes.token)
        assertNull(uCloudEventAttributes.ttl)
        assertNull(uCloudEventAttributes.traceparent)
    }

    @Test
    @DisplayName("Test the isEmpty when built with blank strings function")
    fun test_Isempty_function_when_built_with_blank_strings() {
        val uCloudEventAttributes = uCloudEventAttributes {
            hash = "  "
            token = "  "
        }
        assertTrue(uCloudEventAttributes.isEmpty)
        assertNull(uCloudEventAttributes.hash)
        assertNull(uCloudEventAttributes.priority)
        assertNull(uCloudEventAttributes.token)
        assertNull(uCloudEventAttributes.ttl)
    }

    @Test
    @DisplayName("Test the isEmpty permutations")
    fun test_Isempty_function_permutations() {
        val uCloudEventAttributes= uCloudEventAttributes {
            hash = "  "
            token = "  "
        }
        assertTrue(uCloudEventAttributes.isEmpty)
        val uCloudEventAttributes2 = uCloudEventAttributes {
            hash = "someHash"
            token = "  "
        }
        assertFalse(uCloudEventAttributes2.isEmpty)
        val uCloudEventAttributes3 = uCloudEventAttributes {
            hash = "  "
            token = "SomeToken"
        }
        assertFalse(uCloudEventAttributes3.isEmpty)
        val uCloudEventAttributes4 = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS0
        }
        assertFalse(uCloudEventAttributes4.isEmpty)
        val uCloudEventAttributes5 = uCloudEventAttributes {
            ttl = 8
        }
        assertFalse(uCloudEventAttributes5.isEmpty)
    }
}