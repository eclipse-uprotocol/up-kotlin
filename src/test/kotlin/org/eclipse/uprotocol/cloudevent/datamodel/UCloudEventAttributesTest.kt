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
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes.UCloudEventAttributesBuilder
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
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash("somehash")
            .withPriority(UPriority.UPRIORITY_CS1)
            .withTtl(3)
            .withToken("someOAuthToken")
            .build()
        val expected = "UCloudEventAttributes{hash='somehash', priority=UPRIORITY_CS1, ttl=3, token='someOAuthToken'}"
        assertEquals(expected, uCloudEventAttributes.toString())
    }


    @Test
    @DisplayName("Make sure the toString works when all properties are filled")
    fun testToStringComplete() {
        val uCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash("somehash")
            .withPriority(UPriority.UPRIORITY_CS1)
            .withTtl(3)
            .withToken("someOAuthToken")
            .withTraceparent("darthvader")
            .build()
        val expected =
            "UCloudEventAttributes{hash='somehash', priority=UPRIORITY_CS1, ttl=3, token='someOAuthToken', traceparent='darthvader'}"
        assertEquals(expected, uCloudEventAttributes.toString())
    }

    @Test
    @DisplayName("Test creating a valid attributes object")
    fun test_create_valid() {
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash("somehash")
            .withPriority(UPriority.UPRIORITY_CS6)
            .withTtl(3)
            .withToken("someOAuthToken")
            .build()
        assertTrue(uCloudEventAttributes.hash().isPresent)
        assertEquals("somehash", uCloudEventAttributes.hash().get())
        assertTrue(uCloudEventAttributes.priority().isPresent)
        assertEquals(UPriority.UPRIORITY_CS6, uCloudEventAttributes.priority().get())
        assertTrue(uCloudEventAttributes.ttl().isPresent)
        assertEquals(3, uCloudEventAttributes.ttl().get())
        assertTrue(uCloudEventAttributes.token().isPresent)
        assertEquals("someOAuthToken", uCloudEventAttributes.token().get())
    }

    @Test
    @DisplayName("Test the isEmpty function")
    fun test_Isempty_function() {
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributes.empty()
        assertTrue(uCloudEventAttributes.isEmpty)
        assertTrue(uCloudEventAttributes.hash().isEmpty)
        assertTrue(uCloudEventAttributes.priority().isEmpty)
        assertTrue(uCloudEventAttributes.token().isEmpty)
        assertTrue(uCloudEventAttributes.ttl().isEmpty)
    }

    @Test
    @DisplayName("Test the isEmpty when built with blank strings function")
    fun test_Isempty_function_when_built_with_blank_strings() {
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash("  ")
            .withToken("  ")
            .build()
        assertTrue(uCloudEventAttributes.isEmpty)
        assertTrue(uCloudEventAttributes.hash().isEmpty)
        assertTrue(uCloudEventAttributes.priority().isEmpty)
        assertTrue(uCloudEventAttributes.token().isEmpty)
        assertTrue(uCloudEventAttributes.ttl().isEmpty)
    }

    @Test
    @DisplayName("Test the isEmpty permutations")
    fun test_Isempty_function_permutations() {
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash("  ")
            .withToken("  ")
            .build()
        assertTrue(uCloudEventAttributes.isEmpty)
        val uCloudEventAttributes2: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash("someHash")
            .withToken("  ")
            .build()
        assertFalse(uCloudEventAttributes2.isEmpty)
        val uCloudEventAttributes3: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withHash(" ")
            .withToken("SomeToken")
            .build()
        assertFalse(uCloudEventAttributes3.isEmpty)
        val uCloudEventAttributes4: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withPriority(UPriority.UPRIORITY_CS0)
            .build()
        assertFalse(uCloudEventAttributes4.isEmpty)
        val uCloudEventAttributes5: UCloudEventAttributes = UCloudEventAttributesBuilder()
            .withTtl(8)
            .build()
        assertFalse(uCloudEventAttributes5.isEmpty)
    }
}